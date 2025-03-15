package solutions.onz.services.imagengine.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.extern.slf4j.Slf4j;
import solutions.onz.services.imagengine.codegen.types.FromInput;
import solutions.onz.services.imagengine.codegen.types.TransformInput;
import solutions.onz.services.imagengine.codegen.types.TransformResult;

@Slf4j
@DgsComponent
public class TransformerFetcher {
    @DgsQuery
    public TransformResult transform(@InputArgument TransformInput input) {
        String image = input.getFrom().getImage();
        FromInput from = input.getFrom();

        if (from.getCrop() != null) {
            log.info("Cropping image: {}", image);
        }

        if (from.getResize() != null) {
            log.info("Resizing image: {} to {}, {}", image, from.getResize().getWidth(), from.getResize().getHeight());
        }

        if (from.getRotate() != null) {
            log.info("Rotating image: {} by {}", image, from.getRotate().getAngle());
        }
        if (from.getStitch() != null) {
            log.info("Stitching images: {} to {} {}", image, from.getStitch().getImages(), from.getStitch().getDirection());
        }

        return TransformResult.newBuilder().success(true).url("someUrl").message("Finished").build();

    }
}
