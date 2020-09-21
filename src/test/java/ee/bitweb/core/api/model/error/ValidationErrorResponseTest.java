package ee.bitweb.core.api.model.error;

import ee.bitweb.core.api.ValidationErrorType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationErrorResponseTest {

    @Test
    void testRowOrderByField() {
        ValidationErrorRow first = new ValidationErrorRow("alpha", null, null);
        ValidationErrorRow second = new ValidationErrorRow("beta", null, null);
        ValidationErrorRow third = new ValidationErrorRow("gamma", null, null);
        ValidationErrorRow fourth = new ValidationErrorRow("gamma", "", null);

        ValidationErrorResponse response = new ValidationErrorResponse(ValidationErrorType.CONSTRAINT_VIOLATION);
        response.getRows().add(second);
        response.getRows().add(third);
        response.getRows().add(fourth);
        response.getRows().add(first);

        assertEquals(4, response.getRows().size());

        validateOrder(response, first, second, third, fourth);
    }

    @Test
    void testOrderByReason() {
        ValidationErrorRow first = new ValidationErrorRow("alpha", "NotEmpty", null);
        ValidationErrorRow second = new ValidationErrorRow("alpha", "notnull", null);
        ValidationErrorRow third = new ValidationErrorRow("gamma", null, null);
        ValidationErrorRow fourth = new ValidationErrorRow("gamma", "Email", null);

        ValidationErrorResponse response = new ValidationErrorResponse(ValidationErrorType.CONSTRAINT_VIOLATION);
        response.getRows().add(fourth);
        response.getRows().add(first);
        response.getRows().add(second);
        response.getRows().add(third);

        assertEquals(4, response.getRows().size());

        validateOrder(response, first, second, third, fourth);
    }

    @Test
    void testOrderByMessage() {
        ValidationErrorRow first = new ValidationErrorRow("alpha", "NotEmpty", "must not be empty");
        ValidationErrorRow second = new ValidationErrorRow("alpha", "NotEmpty", "must not be null");
        ValidationErrorRow third = new ValidationErrorRow("gamma", "Email", null);
        ValidationErrorRow fourth = new ValidationErrorRow("gamma", "Email", "invalid format for email");

        ValidationErrorResponse response = new ValidationErrorResponse(ValidationErrorType.CONSTRAINT_VIOLATION);
        response.getRows().add(third);
        response.getRows().add(second);
        response.getRows().add(first);
        response.getRows().add(fourth);

        assertEquals(4, response.getRows().size());

        validateOrder(response, first, second, third, fourth);
    }

    @Test
    void testDuplicatesAreIgnored() {
        ValidationErrorRow first = new ValidationErrorRow("alpha", "NotEmpty", "must not be empty");
        ValidationErrorRow second = new ValidationErrorRow("alpha", "NotEmpty", "must not be empty");

        ValidationErrorResponse response = new ValidationErrorResponse(ValidationErrorType.CONSTRAINT_VIOLATION);
        response.getRows().add(first);
        response.getRows().add(second);

        assertEquals(1, response.getRows().size());

        validateOrder(response, first);
    }

    private void validateOrder(
            ValidationErrorResponse response,
            ValidationErrorRow... expected
    ) {
        // Convert TreeSet to list, as TreeSet doesn't provide get(int i) method. List will retain the same order as
        // TreeSet has
        List<ValidationErrorRow> list = new ArrayList<>(response.getRows());

        for (int i = 0; i < list.size(); i++) {
            assertEquals(
                    expected[i],
                    list.get(i),
                    String.format("Expected %s, but got %s at index %s", expected[i], list.get(i), i)
            );
        }
    }
}
