package solutions.onz.services.imagengine.engine;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Component for performing various image transformations.
 */
@Slf4j
@Component
public class ImageTransformers {

    /**
     * Resizes the given image to the specified width and height.
     *
     * @param image the image to resize
     * @param width the target width
     * @param height the target height
     * @return the resized image
     */
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

    /**
     * Resizes the given image to the specified width and height <b><i>reactively</i></b>.
     *
     * @param image the image to resize
     * @param width the target width
     * @param height the target height
     * @return a Mono emitting the resized image
     */
    @SuppressWarnings("Duplicates")
    public Mono<Mat> reactiveResize(Mat image, int width, int height) {
        return Mono.fromCallable(() -> {
            log.info("Resizing image to {}x{}", width, height);
            Mat resizedImage = new Mat();
            Imgproc.resize(image, resizedImage, new Size(width, height), 0, 0, Imgproc.INTER_AREA);
            if (resizedImage.empty()) {
                log.error("Failed to resize image");
            } else {
                log.info("Image resized successfully");
            }
            return resizedImage;
        });
    }

    /**
     * Converts the given image to grayscale.
     *
     * @param image the image to convert
     * @return the grayscale image
     */
    public Mat grayscale(Mat image) {
        log.info("Converting image to grayscale");
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        if (grayImage.empty()) {
            log.error("Failed to convert image to grayscale");
        } else {
            log.info("Image converted to grayscale successfully");
        }
        return grayImage;
    }

    /**
     * Converts the given image to grayscale <b><i>reactively</i></b>.
     *
     * @param image the image to convert
     * @return a Mono emitting the grayscale image
     */
    public Mono<Mat> reactiveGrayscale(Mat image) {
        return Mono.fromCallable(() -> {
            log.info("Converting image to grayscale");
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            if (grayImage.empty()) {
                log.error("Failed to convert image to grayscale");
            } else {
                log.info("Image converted to grayscale successfully");
            }
            return grayImage;
        });
    }

    /**
     * Converts the given image to sepia.
     *
     * @param image the image to convert
     * @return the sepia image
     */
    public Mat sepia(Mat image) {
        log.info("Converting image to sepia");
        Mat sepiaImage = new Mat();
        Mat sepiaKernel = new Mat(3, 3, CvType.CV_32F);
        sepiaKernel.put(0, 0, 0.272, 0.534, 0.131);
        sepiaKernel.put(1, 0, 0.349, 0.686, 0.168);
        sepiaKernel.put(2, 0, 0.393, 0.769, 0.189);
        Core.transform(image, sepiaImage, sepiaKernel);
        if (sepiaImage.empty()) {
            log.error("Failed to convert image to sepia");
        } else {
            log.info("Image converted to sepia successfully");
        }
        return sepiaImage;
    }

    /**
     * Converts the given image to sepia <b><i>reactively</i></b>.
     *
     * @param image the image to convert
     * @return a Mono emitting the sepia image
     */
    public Mono<Mat> reactiveSepia(Mat image) {
        return Mono.fromCallable(() -> {
            log.info("Converting image to sepia");
            Mat sepiaImage = new Mat();
            Mat sepiaKernel = new Mat(3, 3, CvType.CV_32F);
            sepiaKernel.put(0, 0, 0.272, 0.534, 0.131);
            sepiaKernel.put(1, 0, 0.349, 0.686, 0.168);
            sepiaKernel.put(2, 0, 0.393, 0.769, 0.189);
            Core.transform(image, sepiaImage, sepiaKernel);
            if (sepiaImage.empty()) {
                log.error("Failed to convert image to sepia");
            } else {
                log.info("Image converted to sepia successfully");
            }
            return sepiaImage;
        });
    }

    /**
     * Crops the given image to the specified region.
     *
     * @param image the image to crop
     * @param x the x-coordinate of the top-left corner
     * @param y the y-coordinate of the top-left corner
     * @param width the width of the region
     * @param height the height of the region
     * @return the cropped image
     */
    public Mat crop(Mat image, int x, int y, int width, int height) {
        log.info("Cropping image to {}x{} at ({}, {})", width, height, x, y);
        Mat croppedImage = new Mat(image, new org.opencv.core.Rect(x, y, width, height));
        if (croppedImage.empty()) {
            log.error("Failed to crop image");
        } else {
            log.info("Image cropped successfully");
        }
        return croppedImage;
    }

    /**
     * Crops the given image to the specified region <b><i>reactively</i></b>.
     *
     * @param image the image to crop
     * @param x the x-coordinate of the top-left corner
     * @param y the y-coordinate of the top-left corner
     * @param width the width of the region
     * @param height the height of the region
     * @return a Mono emitting the cropped image
     */
    public Mono<Mat> reactiveCrop(Mat image, int x, int y, int width, int height) {
        return Mono.fromCallable(() -> {
            log.info("Cropping image to {}x{} at ({}, {})", width, height, x, y);
            Mat croppedImage = new Mat(image, new org.opencv.core.Rect(x, y, width, height));
            if (croppedImage.empty()) {
                log.error("Failed to crop image");
            } else {
                log.info("Image cropped successfully");
            }
            return croppedImage;
        });
    }

    /**
     * Rotates the given image by the specified angle.
     *
     * @param image the image to rotate
     * @param angle the angle to rotate by
     * @return the rotated image
     */
    public Mat rotate(Mat image, double angle) {
        log.info("Rotating image by {} degrees", angle);
        Mat rotatedImage = new Mat();
        Point center = new Point(image.width() / 2.0, image.height() / 2.0);
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        Imgproc.warpAffine(image, rotatedImage, rotationMatrix, new Size(image.width(), image.height()));
        if (rotatedImage.empty()) {
            log.error("Failed to rotate image");
        } else {
            log.info("Image rotated successfully");
        }
        return rotatedImage;
    }

    /**
     * Rotates the given image by the specified angle <b><i>reactively</i></b>.
     *
     * @param image the image to rotate
     * @param angle the angle to rotate by
     * @return a Mono emitting the rotated image
     */
    public Mono<Mat> reactiveRotate(Mat image, double angle) {
        return Mono.fromCallable(() -> {
            log.info("Rotating image by {} degrees", angle);
            Mat rotatedImage = new Mat();
            Point center = new Point(image.width() / 2.0, image.height() / 2.0);
            Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
            Imgproc.warpAffine(image, rotatedImage, rotationMatrix, new Size(image.width(), image.height()));
            if (rotatedImage.empty()) {
                log.error("Failed to rotate image");
            } else {
                log.info("Image rotated successfully");
            }
            return rotatedImage;
        });
    }
}