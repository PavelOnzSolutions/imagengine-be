package solutions.onz.services.imagengine.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.domain.enums.ImageFormat;
import solutions.onz.services.imagengine.services.ImageTransformerService;
import solutions.onz.services.imagengine.services.TransformerRestQueryParserService;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/transform/image")
public class ImageTransformerController {
    private final ImageTransformerService imageTransformerService;
    private final TransformerRestQueryParserService transformerRestQueryParserService;

    public ImageTransformerController(
            ImageTransformerService imageTransformerService,
            TransformerRestQueryParserService transformerRestQueryParserService) {
        this.imageTransformerService = imageTransformerService;
        this.transformerRestQueryParserService = transformerRestQueryParserService;
    }

    /**
     * Simple transformation endpoint.
     *.body(new InputStreamResource(Files.newInputStream(file.toPath()))))
                     .onErrorMap(IOException.class, e -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read the file", e))
     * @param query
     * @param source
     * @return
     */
    @Operation(
            summary = "Simple image transformation endpoint",
            description = "Simple transformation endpoint. Takes operation name (e.g. resize, crop etc), query containing operation-specific parameters and source parameter (URL or stored image ID).",
            tags = {"Image Transformation API"},
            operationId = "simpleTransform")
    @GetMapping(path = "/{query}/{output}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<InputStreamResource>> simpleTransform(
            @PathVariable String query,
            @PathVariable ImageFormat output,
            @RequestParam(name = "source") String source
    ) {
        return transformerRestQueryParserService.parseFormula(query)
                .flatMap(operationsWithParams -> imageTransformerService.transformAndReturnImage(source, operationsWithParams, output))
                .flatMap(imageBytes -> {
                    InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(imageBytes));
                    HttpHeaders headers = new HttpHeaders();

                    try {
                        headers.setContentDisposition(
                                ContentDisposition
                                        .attachment()
                                        .filename(URI.create(source).toURL().getFile() + "_transformed_" + Instant.now() + "." + output.toString())
                                        .build()
                        );
                    } catch (MalformedURLException e) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid source URL"));
                    }

                    return Mono.just(ResponseEntity
                            .ok()
                            .headers(headers)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .contentLength(imageBytes.length)
                            .body(resource));
                })
                .doOnError(error -> {
                    log.error("Error during image transformation: {}", error.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image transformation failed: " + error.getMessage());
                });
    }
}
