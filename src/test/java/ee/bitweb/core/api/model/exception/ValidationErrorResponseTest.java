package ee.bitweb.core.api.model.exception;

import ee.bitweb.core.api.ValidationErrorType;
import ee.bitweb.core.exception.validation.FieldError;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationErrorResponseTest {

    @Test
    void testRowOrderByField() {
        FieldErrorResponse first = new FieldErrorResponse("alpha", null, null);
        FieldErrorResponse second = new FieldErrorResponse("beta", null, null);
        FieldErrorResponse third = new FieldErrorResponse("gamma", null, null);
        FieldErrorResponse fourth = new FieldErrorResponse("gamma", "", null);

        ValidationErrorResponse response = new ValidationErrorResponse(
                "1",
                ValidationErrorType.CONSTRAINT_VIOLATION.toString(),
                List.of(
                        second,
                        third,
                        fourth,
                        first
                )
        );

        assertEquals(4, response.getErrors().size());

        validateOrder(response, first, second, third, fourth);
    }

    @Test
    void testOrderByReason() {
        FieldErrorResponse first = new FieldErrorResponse("alpha", "NotEmpty", null);
        FieldErrorResponse second = new FieldErrorResponse("alpha", "notnull", null);
        FieldErrorResponse third = new FieldErrorResponse("gamma", null, null);
        FieldErrorResponse fourth = new FieldErrorResponse("gamma", "Email", null);

        ValidationErrorResponse response = new ValidationErrorResponse(
                "1",
                ValidationErrorType.CONSTRAINT_VIOLATION.toString(),
                List.of(
                        second,
                        third,
                        fourth,
                        first
                )
        );


        assertEquals(4, response.getErrors().size());

        validateOrder(response, first, second, third, fourth);
    }

    @Test
    void testOrderByMessage() {
        FieldErrorResponse first = new FieldErrorResponse("alpha", "NotEmpty", "must not be empty");
        FieldErrorResponse second = new FieldErrorResponse("alpha", "NotEmpty", "must not be null");
        FieldErrorResponse third = new FieldErrorResponse("gamma", "Email", null);
        FieldErrorResponse fourth = new FieldErrorResponse("gamma", "Email", "invalid format for email");

        ValidationErrorResponse response = new ValidationErrorResponse(
                "1",
                ValidationErrorType.CONSTRAINT_VIOLATION.toString(),
                List.of(
                        second,
                        third,
                        fourth,
                        first
                )
        );

        assertEquals(4, response.getErrors().size());

        validateOrder(response, first, second, third, fourth);
    }

    @Test
    void testDuplicatesAreIgnored() {
        FieldErrorResponse first = new FieldErrorResponse("alpha", "NotEmpty", "must not be empty");
        FieldErrorResponse second = new FieldErrorResponse("alpha", "NotEmpty", "must not be empty");

        ValidationErrorResponse response = new ValidationErrorResponse(
                "1",
                ValidationErrorType.CONSTRAINT_VIOLATION.toString(),
                List.of(
                        first,
                        second
                )
        );

        assertEquals(1, response.getErrors().size());

        validateOrder(response, first);
    }

    private void validateOrder(
            ValidationErrorResponse response,
            FieldErrorResponse... expected
    ) {
        // Convert TreeSet to list, as TreeSet doesn't provide get(int i) method. List will retain the same order as
        // TreeSet has
        List<FieldErrorResponse> list = new ArrayList<>(response.getErrors());

        for (int i = 0; i < list.size(); i++) {
            assertEquals(
                    expected[i],
                    list.get(i),
                    String.format("Expected %s, but got %s at index %s", expected[i], list.get(i), i)
            );
        }
    }
}
