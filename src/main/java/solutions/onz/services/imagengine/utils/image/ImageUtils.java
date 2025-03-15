package solutions.onz.services.imagengine.utils.image;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

@Slf4j
public class ImageUtils {

    /**
     * Converts a {@link BufferedImage} to a Base64 encoded {@link String}
     * @param image a {@link BufferedImage}
     * @return a Base64 encoded {@link String} representing the specified image
     */
    public static byte[] imageToBase64String(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException exception) {
            log.error("Error processing image");
            return new byte[0];
        }
        return Base64.getEncoder().encode(baos.toByteArray());
    }

    /**
     * Scales an image by a given factor
     * @param image the {@link BufferedImage} to scale
     * @param scaleFactor the scale factor (&lt;1 will scale down, &gt;1 will scale up, 1 = 100%)
     * @return a {@link BufferedImage} which is a version of the original image scaled by the given
     *     factor
     */
    public static BufferedImage scaleImage(BufferedImage image, double scaleFactor) {
        int newWidth = (int) (image.getWidth() * scaleFactor);
        int newHeight = (int) (image.getHeight() * scaleFactor);
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        image = outputImage;
        return image;
    }

    /**
     * Scales a {@link Mat} image by the given factor
     * @param image the {@link Mat} to scale
     * @param scaleFactor the scale factor (&lt;1 will scale down, &gt;1 will scale up, 1 = 100%)
     * @return a {@link Mat} which is a version of the original image scaled by the given factor
     */
    public static Mat scaleImage(Mat image, double scaleFactor) {
        Mat resizedImage = new Mat();
        Size sz = new Size(image.width() * scaleFactor, image.height() * scaleFactor);
        Imgproc.resize(image, resizedImage, sz);
        return resizedImage;
    }
}
