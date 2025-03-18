package solutions.onz.services.imagengine.services;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import solutions.onz.services.imagengine.config.ApplicationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;


@Slf4j
@Service
public class ImageStorageService {
    private final ApplicationProperties applicationProperties;

    public ImageStorageService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Saves a Mat image to the filesystem with the specified format
     *
     * @param image  The OpenCV Mat image to save
     * @param format The image format (jpg, png, etc.)
     * @return Mono emitting the path to the saved file
     */
    public Mono<String> saveImage(Mat image, String format) {
        return Mono.fromCallable(() -> {
                    String fileName = generateUniqueFileName(format);
                    String filePath = applicationProperties.getStorage().getPath() + "/" + fileName;

                    MatOfByte matOfByte = new MatOfByte();
                    Imgcodecs.imencode("." + format, image, matOfByte);
                    byte[] byteArray = matOfByte.toArray();

                    Path path = Paths.get(filePath);
                    Files.createDirectories(path.getParent());
                    Files.write(path, byteArray);

                    log.info("Image saved successfully at: {}", filePath);
                    return filePath;
                }).subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> log.error("Failed to save image: {}", error.getMessage()));
    }

    /**
     * Saves a byte array image to the filesystem with the specified filename and extension
     *
     * @param image     byte array of the image
     * @param filename  name of the file
     * @param extension file extension
     * @return Mono emitting the path to the saved file
     */
    public Mono<String> saveImage(byte[] image, String filename, String extension) {
        return Mono.fromCallable(() -> {
                    String fileName = filename + "." + extension;
                    String filePath = applicationProperties.getStorage().getPath() + "/" + fileName;
                    Path path = Paths.get(filePath);
                    Files.createDirectories(path.getParent());
                    Files.write(path, image, StandardOpenOption.CREATE);

                    log.info("Image saved successfully at: {}", filePath);
                    return filePath;
                }).subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> log.error("Failed to save image: {}", error.getMessage()));
    }

    /**
     * Deletes an image from the filesystem
     * @param filename name of the file to delete
     * @return Mono emitting when the image is deleted
     */
    public Mono<Void> deleteImage(String filename) {
        return Mono.fromCallable(() -> {
            Path path = Paths.get(applicationProperties.getStorage().getPath() + "/" + filename);
            Files.deleteIfExists(path);
            return null;
        }).subscribeOn(Schedulers.boundedElastic())
          .doOnError(error -> log.error("Failed to delete image: {}", error.getMessage())).then();
    }

    /**
     * Retrieves an image from the filesystem as a byte stream
     * @param filename name of the file to retrieve
     * @return Flux emitting the image as a byte array
     */
    public Flux<Byte> getImage(String filename) {
        Path fik = Paths.get(applicationProperties.getStorage().getPath() + "/" + filename);
        return Mono.fromCallable(() -> Files.readAllBytes(fik))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(byteArray -> {
                    Byte[] byteObjectArray = new Byte[byteArray.length];
                    for (int i = 0; i < byteArray.length; i++) {
                        byteObjectArray[i] = byteArray[i]; // Autoboxing
                    }
                    return Flux.fromArray(byteObjectArray);
                })
                .doOnError(e -> log.error("Failed to retrieve image: {}", e.getMessage()));
    }


    /**
     * Generates a unique filename based on timestamp and random UUID
     */
    private String generateUniqueFileName(String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + randomPart + "." + format;
    }
}
