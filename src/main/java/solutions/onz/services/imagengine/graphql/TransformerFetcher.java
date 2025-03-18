package solutions.onz.services.imagengine.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.codegen.types.*;
import solutions.onz.services.imagengine.graphql.exception.ReactiveImageTransformerException;
import solutions.onz.services.imagengine.services.ImageTransformerService;


@Slf4j
@DgsComponent
public class TransformerFetcher {
    private final ImageTransformerService imageTransformerService;

    public TransformerFetcher(ImageTransformerService imageTransformerService) {
        this.imageTransformerService = imageTransformerService;
    }

    @DgsQuery
    public Mono<TransformResult> transform(@InputArgument TransformerInput image) {
        return imageTransformerService.transform(image)
                .onErrorResume(e -> Mono.error(new ReactiveImageTransformerException("Failed to transform image: " + e.getMessage())));

    }

    @DgsQuery
    public Mono<ConverterResult> convert(@InputArgument ConverterInput images) {

        return Mono.empty();
    }
}
