package solutions.onz.services.imagengine.services;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import solutions.onz.services.imagengine.codegen.types.*;
import solutions.onz.services.imagengine.domain.ImageEntity;
import solutions.onz.services.imagengine.domain.enums.ImageFormat;
import solutions.onz.services.imagengine.engine.ImageTransformers;
import solutions.onz.services.imagengine.graphql.exception.ReactiveImageTransformerException;
import solutions.onz.services.imagengine.repository.ImageRepository;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Service for transforming images.
 */
@Slf4j
@Service
public class ImageTransformerService {
    private final WebClient webClient;
    private final ImageTransformers imageTransformers;
    private final DeepLinkService deepLinkService;
    private final ImageStorageService imageStorageService;
    private final ImageRepository imageRepository;

    /**
     * Constructor for ImageTransformerService.
     *
     * @param webClient the WebClient for making HTTP requests
     * @param imageTransformers the service for applying image transformations
     * @param deepLinkService the service for managing deep links
     * @param imageStorageService the service for storing images
     */
    public ImageTransformerService(
            WebClient webClient,
            ImageTransformers imageTransformers,
            DeepLinkService deepLinkService,
            ImageStorageService imageStorageService,
            ImageRepository imageRepository) {
        this.webClient = webClient;
        this.imageTransformers = imageTransformers;
        this.deepLinkService = deepLinkService;
        this.imageStorageService = imageStorageService;
        this.imageRepository = imageRepository;
    }

    /**
     * Transforms an image based on the given input.
     *
     * @param input the transformer input containing from/to specs
     * @return a Mono emitting the transformation result
     */
    public Mono<TransformResult> transformGraphql(TransformerInput input) {
        return applyTransformers(input.getFrom())
                .flatMap(mat -> imageStorageService.saveImage(mat, input.getTo().getFormat().toString()).flatMap(Mono::just))
                .flatMap(deepLinkService::createLink)
                .map(deepLink -> TransformResult.newBuilder()
                        .url("http://localhost:8080/sink/" + deepLink.getShortcut())
                        .success(true)
                        .message("Image transformed successfully")
                        .build())
                .doOnError(err -> log.error("Error transforming image: {}", err.getMessage()));
    }

    /**
     * Transforms an image based on the given input.
      * @param imageLinkOrId the image link or ID
     * @param operationsWithParams the operations to apply to the image
     * @return a Mono emitting the transformed image
     */
    public Mono<byte[]> transformAndReturnImage(String imageLinkOrId, Map<String, Map<String, String>> operationsWithParams, ImageFormat outputFormat) {
        Mono<Mat> imagePathMono;

        if (isImageLink(imageLinkOrId)) {
            imagePathMono = getImageFromUrl(imageLinkOrId);
        } else {
            imagePathMono = imageRepository.findById(imageLinkOrId)
                    .map(ImageEntity::getFilePath)
                    .flatMap(this::getImageFromUrl)
                    .switchIfEmpty(Mono.error(new ReactiveImageTransformerException("Image not found")));
        }

        return imagePathMono.flatMap(img -> {
            Mono<Mat> result = Mono.just(img);
            for (Map.Entry<String, Map<String, String>> entry : operationsWithParams.entrySet()) {
                log.info("Applying operation: {}", entry);
                String transformerName = entry.getKey();
                Map<String, String> params = entry.getValue();
                result = switch (transformerName) {
                    case "RESIZE" -> {
                        if (params.containsKey("width") && params.containsKey("height") && !params.get("width").isBlank() && !params.get("height").isBlank()) {
                            yield result.flatMap(mat -> imageTransformers.reactiveResize(mat, Integer.parseInt(params.get("width")), Integer.parseInt(params.get("height"))));
                        } else {
                            yield Mono.error(new ReactiveImageTransformerException("Resize transformer requires width and height parameters"));
                        }
                    }
                    case "GRAYSCALE" -> result.flatMap(imageTransformers::reactiveGrayscale);
                    case "SEPIA" -> result.flatMap(imageTransformers::reactiveSepia);
                    case "CROP" -> {
                        if (
                                params.containsKey("x") &&
                                params.containsKey("y") &&
                                params.containsKey("width") &&
                                params.containsKey("height") &&
                                !params.get("x").isBlank() &&
                                !params.get("y").isBlank() &&
                                !params.get("width").isBlank() &&
                                !params.get("height").isBlank()
                        ) {
                            yield result.flatMap(mat -> imageTransformers.reactiveCrop(mat, Integer.parseInt(params.get("x")), Integer.parseInt(params.get("y")), Integer.parseInt(params.get("width")), Integer.parseInt(params.get("height"))));
                        } else {
                            yield Mono.error(new ReactiveImageTransformerException("Crop transformer requires x, y, width, and height parameters"));
                        }
                    }
                    case "ROTATE" -> {
                        if (params.containsKey("angle") && !params.get("angle").isBlank()) {
                            yield result.flatMap(mat -> imageTransformers.reactiveRotate(mat, Double.parseDouble(params.get("angle"))));
                        } else {
                            yield Mono.error(new ReactiveImageTransformerException("Rotate transformer requires an angle parameter"));
                        }
                    }
                    default -> result;
                };
            }
            return result;
        }).flatMap(transformedMat -> {
            MatOfByte matOfByte = new MatOfByte();
            if (!Imgcodecs.imencode("." + outputFormat.toString(), transformedMat, matOfByte)) {
                return Mono.error(new ReactiveImageTransformerException("Failed to encode transformed image to byte array"));
            }
            return Mono.just(matOfByte.toArray());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Applies the specified transformations to the image.
     *
     * @param input the input containing the source and transformations
     * @return a Mono emitting the transformed image
     */
    public Mono<Mat> applyTransformers(FromInput input) {
        return Mono.just(input.getSource())
                .flatMap(this::getImageFromUrl)
                .flatMap(img -> {
                    Mono<Mat> result = Mono.just(img);
                    for (Transformers transformer : input.getTransformers()) {
                        switch (transformer) {
                            case RESIZE: {
                                if (null != input.getResize()) {
                                    result = result.flatMap(mat -> imageTransformers.reactiveResize(mat, input.getResize().getWidth(), input.getResize().getHeight()));
                                } else {
                                    return Mono.error(() -> new ReactiveImageTransformerException("Resize transformer requires width and height"));
                                }
                                break;
                            }
                            case GRAYSCALE: {
                                result = result.flatMap(imageTransformers::reactiveGrayscale);
                                break;
                            }
                            case SEPIA: {
                                result = result.flatMap(imageTransformers::reactiveSepia);
                                break;
                            }
                            case CROP: {
                                if (null != input.getCrop()) {
                                    result = result.flatMap(mat -> imageTransformers.reactiveCrop(mat, input.getCrop().getX(), input.getCrop().getY(), input.getCrop().getWidth(), input.getCrop().getHeight()));
                                } else {
                                    return Mono.error(() -> new ReactiveImageTransformerException("Crop transformer requires crop parameters(x, y, width and height)"));
                                }
                                break;
                            }
                            case ROTATE: {
                                if (null != input.getRotate()) {
                                    result = result.flatMap(mat -> imageTransformers.reactiveRotate(mat, input.getRotate().getAngle()));
                                } else {
                                    return Mono.error(() -> new ReactiveImageTransformerException("Rotate transformer requires an angle"));
                                }
                            }
                            case STITCH:
                            case STICKER:
                            case WATERMARK:
                                // Implement other transformations as needed
                                break;
                        }
                    }
                    return result;
                })
                .doOnError(error -> log.error("Error transforming image: {}", error.getMessage()));
    }


    /**
     * Converts the source input to a different format.
     *
     * @param input the source input
     * @return a Mono emitting the conversion result
     */
    public Mono<ConverterResult> convert(SourceInput input) {
        return Mono.empty();
    }

    /**
     * Outputs the transformed image to the specified location.
     *
     * @param spec the output specification
     * @return a Mono emitting the output path
     */
    public Mono<String> output(ToInput spec) {
        return Mono.empty();
    }

    /**
     * Retrieves an image from the given URL.
     *
     * @param uri the URL of the image
     * @return a Mono emitting the image as a Mat object
     */
    public Mono<Mat> getImageFromUrl(String uri) {
        return webClient
                .method(HttpMethod.GET)
                .uri(uri)
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(this::reactiveConvertImageByteDataToMat)
                .doOnNext(data -> log.info("Received image of size: {}x{}", data.width(), data.height()))
                .doOnError(throwable -> log.error("Error converting image: {}", throwable.getMessage()));
    }


    private Boolean isImageLink(String imageLinkOrId) {
        try {
            new URI(imageLinkOrId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts byte array data to a Mat object.
     *
     * @param data the byte array data
     * @return the Mat object
     */
    private Mat convertImageByteDataToMat(byte[] data) {
        Mat mat = Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.IMREAD_COLOR);
        if (mat.empty()) {
            throw new IllegalArgumentException("Failed to convert byte array to Mat");
        }
        return mat;
    }

    /**
     * Reactively converts byte array data to a Mat object.
     *
     * @param data the byte array data
     * @return a Mono emitting the Mat object
     */
    private Mono<Mat> reactiveConvertImageByteDataToMat(byte[] data) {
        return Mono.fromCallable(() -> {
            Mat mat = Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.IMREAD_COLOR);
            if (mat.empty()) {
                throw new IllegalArgumentException("Failed to convert byte array to Mat");
            }
            return mat;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}