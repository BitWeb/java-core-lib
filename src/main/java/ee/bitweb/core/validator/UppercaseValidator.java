package ee.bitweb.core.validator;

import org.springframework.util.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UppercaseValidator implements ConstraintValidator<Uppercase, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) return true;

        for (var i = 0; i < value.length(); i++) {
            if (Character.isLowerCase(value.charAt(i))) return false;
        }

        return true;
    }
}
