package solutions.onz.services.imagengine.engine;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImageTransformers {
    public Mat resize(Mat image, int width, int height) {
        log.info("Resizing image to {}x{}", width, height);
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new Size(width, height));
        if (resizedImage.empty()) {
            log.error("Failed to resize image");
        } else {
            log.info("Image resized successfully");
        }
        return resizedImage;
    }
}
