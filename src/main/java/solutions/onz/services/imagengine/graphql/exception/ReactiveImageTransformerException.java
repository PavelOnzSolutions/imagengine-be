package solutions.onz.services.imagengine.graphql.exception;

import com.netflix.graphql.dgs.exceptions.DgsBadRequestException;
import org.jetbrains.annotations.NotNull;

public class ReactiveImageTransformerException extends DgsBadRequestException {
    public ReactiveImageTransformerException() {
        super();
    }

    public ReactiveImageTransformerException(@NotNull String message) {
        super(message);
    }
}
