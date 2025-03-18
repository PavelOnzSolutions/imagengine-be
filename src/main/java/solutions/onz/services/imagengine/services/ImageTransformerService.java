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
import solutions.onz.services.imagengine.engine.ImageTransformers;
import solutions.onz.services.imagengine.graphql.exception.ReactiveImageTransformerException;

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
            ImageStorageService imageStorageService) {
        this.webClient = webClient;
        this.imageTransformers = imageTransformers;
        this.deepLinkService = deepLinkService;
        this.imageStorageService = imageStorageService;
    }

    /**
     * Transforms an image based on the given input.
     *
     * @param input the transformer input containing from/to specs
     * @return a Mono emitting the transformation result
     */
    public Mono<TransformResult> transform(TransformerInput input) {
        return applyTransformers(input.getFrom())
                .flatMap(mat -> {
                    log.info("Resulting image has dimensions: {}x{} and size: {}", mat.width(), mat.height(), mat.total());

                    return imageStorageService.saveImage(mat, input.getTo().getFormat().toString()).flatMap(Mono::just);
                })
                .map(outPath -> TransformResult.newBuilder()
                        .url(outPath)
                        .success(true)
                        .message("Image transformed successfully")
                        .build())
                .doOnError(err -> log.error("Error transforming image: {}", err.getMessage()));
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
                    for (Transformers transformer : input.getTransformers()) {
                        switch (transformer) {
                            case RESIZE -> {
                                if (null != input.getResize()) {
                                    return resizeImage(Mono.just(img), input.getResize().getWidth(), input.getResize().getHeight());
                                } else {
                                    return Mono.error(() -> new ReactiveImageTransformerException("Resize transformer requires width and height"));
                                }
                            }
                        }
                    }

                    return Mono.just(img);
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

    /**
     * Resizes the given image to the specified dimensions.
     *
     * @param matMono the Mono emitting the image to be resized
     * @param width the target width
     * @param height the target height
     * @return a Mono emitting the resized image
     */
    private Mono<Mat> resizeImage(Mono<Mat> matMono, int width, int height) {
        return matMono.map(mat -> imageTransformers.resize(mat, width, height));
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