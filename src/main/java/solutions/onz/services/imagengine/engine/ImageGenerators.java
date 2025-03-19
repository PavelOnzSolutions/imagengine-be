package solutions.onz.services.imagengine.engine;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.springframework.stereotype.Component;
import solutions.onz.services.imagengine.codegen.types.StitchDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ImageGenerators { /*
    public Mat stitch(Mat origin, StitchDirection direction, Mat... images) {
        log.info("Stitching images in {} direction", direction);

        // If no additional images provided, return the origin
        if (images == null || images.length == 0) {
            log.info("No additional images provided for stitching");
            return origin;
        }

        List<Mat> allImages = new ArrayList<>();
        allImages.add(origin);
        Collections.addAll(allImages, images);

        try {
            // Create a stitcher with default parameters
            Stitcher stitcher = Stitcher.create(Stitcher.PANORAMA);

            // Configure stitcher based on direction if needed
            // Different directions might need different configuration

            Mat result = new Mat();
            Stitcher.Status status = stitcher.stitch(allImages, result);

            if (status != Stitcher.Status.OK) {
                log.error("Stitching failed with status: {}", status);
                return origin;
            }

            if (result.empty()) {
                log.error("Stitched result is empty");
                return origin;
            } else {
                log.info("Images stitched successfully");
                return result;
            }
        } catch (Exception e) {
            log.error("Error stitching images: {}", e.getMessage(), e);
            return origin;
        }
    }*/
}
