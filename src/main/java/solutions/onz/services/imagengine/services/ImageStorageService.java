package solutions.onz.services.imagengine.services;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import solutions.onz.services.imagengine.config.ApplicationProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
         * @param image The OpenCV Mat image to save
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
         * Generates a unique filename based on timestamp and random UUID
         */
        private String generateUniqueFileName(String format) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String randomPart = UUID.randomUUID().toString().substring(0, 8);
            return timestamp + "_" + randomPart + "." + format;
        }
}
