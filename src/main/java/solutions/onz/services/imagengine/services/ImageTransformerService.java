package solutions.onz.services.imagengine.services;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.codegen.types.*;
import solutions.onz.services.imagengine.engine.ImageTransformers;
import solutions.onz.services.imagengine.graphql.exception.ReactiveImageTransformerException;

import java.net.URL;

@Slf4j
@Service
public class ImageTransformerService {
    private final WebClient webClient;
    private final ImageTransformers imageTransformers;

    public ImageTransformerService(WebClient webClient, ImageTransformers imageTransformers) {
        this.webClient = webClient;
        this.imageTransformers = imageTransformers;
    }

    /**
     * Transform an image. Takes Query input
     *
     * @param input
     * @return
     */
    public Mono<TransformResult> transform(TransformerInput input) {
        return applyTransformers(input.getFrom())
                .flatMap(mat -> {
                    log.info("Resulting image has dimensions: {}x{} and size: {}", mat.width(), mat.height(), mat.total());

                    String outUrl = "http://test";
                    return Mono.just(outUrl);
                })
                        .map(outPath -> TransformResult.newBuilder()
                                .url(outPath)
                                .success(true)
                                .message("Image transformed successfully")
                                .build())
                .doOnError(err -> log.error("Error transforming image: {}", err.getMessage()));
    }

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

    public Mono<ConverterResult> convert(SourceInput input) {
        return Mono.empty();
    }

    public Mono<String> output(ToInput spec) {
        return Mono.empty();
    }

    public Mono<Mat> getImageFromUrl(String uri) {
        return webClient
                .method(HttpMethod.GET)
                .uri(uri)
                .retrieve()
                .bodyToMono(byte[].class)
                .map(this::convertImageByteDataToMat)
                .doOnNext(data -> log.info("Received image of size: {}x{}", data.width(), data.height()))
                .doOnError(throwable -> log.error("Error converting image: {}", throwable.getMessage()));
    }

    private Mono<Mat> resizeImage(Mono<Mat> matMono, int width, int height) {
        return matMono.map(mat -> imageTransformers.resize(mat, width, height));
    }


    private Mat convertImageByteDataToMat(byte[] data) {
        Mat mat = Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.IMREAD_COLOR);
        if (mat.empty()) {
            throw new IllegalArgumentException("Failed to convert byte array to Mat");
        }
        return mat;
    }


}
