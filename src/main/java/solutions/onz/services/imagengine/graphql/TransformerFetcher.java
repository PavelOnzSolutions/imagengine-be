package solutions.onz.services.imagengine.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.exceptions.DgsBadRequestException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.codegen.types.*;
import solutions.onz.services.imagengine.services.ImageTransformerService;

import java.util.List;

@Slf4j
@DgsComponent
public class TransformerFetcher {
    private final ImageTransformerService imageTransformerService;

    public TransformerFetcher(ImageTransformerService imageTransformerService) {
        this.imageTransformerService = imageTransformerService;
    }

    @DgsQuery
    public Mono<TransformResult> transform(@InputArgument TransformerInput input) {
        String image = input.getFrom().getImage();
        FromInput from = input.getFrom();

        return imageTransformerService.transform(input)
                .onErrorResume(e -> Mono.error(new DgsBadRequestException("Failed to transform image: " + e.getMessage())));

    }

    @DgsQuery
    public Mono<ConverterResult> convert(@InputArgument ConverterInput input) {
        List<String> images = input.getFrom().getImages();
        Format format = input.getTo().getFormat();

        return Mono.empty();
    }
}
