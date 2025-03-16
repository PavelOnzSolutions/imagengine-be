package solutions.onz.services.imagengine.utils.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import solutions.onz.services.imagengine.utils.web.ErrorConstants;
import solutions.onz.services.imagengine.utils.web.ProblemDetailWithCause;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class InvalidPasswordException extends ErrorResponseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidPasswordException() {
        super(
            HttpStatus.BAD_REQUEST,
            ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withType(ErrorConstants.INVALID_PASSWORD_TYPE)
                .withTitle("Incorrect password")
                .build(),
            null
        );
    }
}
