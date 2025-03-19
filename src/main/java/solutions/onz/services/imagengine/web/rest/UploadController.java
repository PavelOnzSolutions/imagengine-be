package solutions.onz.services.imagengine.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.config.ApplicationProperties;
import solutions.onz.services.imagengine.domain.ImageEntity;
import solutions.onz.services.imagengine.repository.ImageRepository;
import solutions.onz.services.imagengine.services.ImageStorageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/image-upload")
public class UploadController {
    private final ImageRepository imageRepository;
    private final ApplicationProperties applicationProperties;
    private final ImageStorageService imageStorageService;


    public UploadController(
            ImageRepository imageRepository,
            ApplicationProperties applicationProperties,
            ImageStorageService imageStorageService) {
        this.imageRepository = imageRepository;
        this.applicationProperties = applicationProperties;
        this.imageStorageService = imageStorageService;
    }

    @Operation(summary = "Upload an image", description = "Upload an image to internal image store.", tags = {"Image API"}, operationId = "uploadImage")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, name = "uploadImage")
    public Mono<ResponseEntity<Iterable<ImageEntity>>> uploadImage(@RequestBody MultipartFile file) {
        log.debug("REST POST request to import Competences from JSON");

        if (null == file) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file provided"));
        }

        return Mono.empty();
    }
}
