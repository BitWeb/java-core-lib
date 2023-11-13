package ee.bitweb.core.exception.validation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * InvalidFormatValidationException.class encapsulates InvalidFormatException in order to gain access to path, value
 * and target class of failed formatting. This enables generation of proper validation error message.
 */
@Slf4j
@Getter
public class InvalidFormatValidationException extends InvalidFormatException {

    public static final String UNKNOWN_VALUE = "Unknown";

    private final String field;

    private final Object value;

    private final Class<?> targetClass;

    public InvalidFormatValidationException(InvalidFormatException exception) {
        super((JsonParser) exception.getProcessor(), exception.getMessage(), exception.getValue(), exception.getTargetType());

        value = exception.getValue();
        field = parseFieldName(exception.getPath());
        targetClass = exception.getTargetType();
    }

    public InvalidFormatValidationException(MismatchedInputException exception) {
        super(
                (JsonParser) exception.getProcessor(),
                exception.getMessage(),
                UNKNOWN_VALUE,
                exception.getTargetType()
        );
        value = UNKNOWN_VALUE;
        field = parseFieldName(exception.getPath());
        targetClass = exception.getTargetType();
    }

    private String parseFieldName(List<Reference> references) {
        ArrayList<String> fieldNames = new ArrayList<>();
        for (Reference r : references) {
            if (StringUtils.hasText(r.getFieldName())) {
                fieldNames.add(r.getFieldName());
            }
        }

        return String.join(".", fieldNames);
    }
}
