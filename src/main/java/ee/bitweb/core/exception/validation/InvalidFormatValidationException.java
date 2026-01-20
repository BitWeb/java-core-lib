package ee.bitweb.core.exception.validation;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * InvalidFormatValidationException.class encapsulates InvalidFormatException in order to gain access to path, value
 * and target class of failed formatting. This enables generation of proper validation error message.
 */
@Slf4j
@Getter
public class InvalidFormatValidationException extends RuntimeException {

    public static final String UNKNOWN_VALUE = "Unknown";

    private final String field;

    private final transient Object value;

    private final Class<?> targetClass;

    public InvalidFormatValidationException(InvalidFormatException exception) {
        super(exception.getMessage(), exception);

        value = exception.getValue();
        field = parseFieldName(exception.getPath());
        targetClass = exception.getTargetType();
    }

    public InvalidFormatValidationException(MismatchedInputException exception) {
        super(exception.getMessage(), exception);

        value = UNKNOWN_VALUE;
        field = parseFieldName(exception.getPath());
        targetClass = exception.getTargetType();
    }

    private String parseFieldName(List<JacksonException.Reference> references) {
        ArrayList<String> fieldNames = new ArrayList<>();
        for (JacksonException.Reference r : references) {
            if (StringUtils.hasText(r.getPropertyName())) {
                fieldNames.add(r.getPropertyName());
            }
        }

        return String.join(".", fieldNames);
    }
}
