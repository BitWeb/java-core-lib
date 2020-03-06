package ee.bitweb.core.api.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class UppercaseValidator implements ConstraintValidator<Uppercase, String> {

    private final Pattern pattern = Pattern.compile("[A-Z0-9_]+");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;

        return pattern.matcher(value).matches();
    }
}
