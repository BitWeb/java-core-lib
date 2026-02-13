package ee.bitweb.core.api.model.exception;

import ee.bitweb.core.exception.persistence.Criteria;
import ee.bitweb.core.exception.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PersistenceErrorResponseTest {

    @Test
    @DisplayName("Should create from PersistenceException")
    void shouldCreateFromPersistenceException() {
        EntityNotFoundException exception = new EntityNotFoundException(
                "User not found",
                "User",
                "id",
                "123"
        );

        PersistenceErrorResponse response = new PersistenceErrorResponse("trace-1", exception);

        assertAll(
                () -> assertEquals("trace-1", response.getId()),
                () -> assertEquals("User not found", response.getMessage()),
                () -> assertEquals("User", response.getEntity()),
                () -> assertEquals(1, response.getCriteria().size())
        );
    }

    @Test
    @DisplayName("Should create with direct parameters")
    void shouldCreateWithDirectParameters() {
        Set<Criteria> criteria = Set.of(
                new Criteria("id", "123"),
                new Criteria("status", "ACTIVE")
        );

        PersistenceErrorResponse response = new PersistenceErrorResponse(
                "trace-2",
                "Entity not found",
                "User",
                criteria
        );

        assertAll(
                () -> assertEquals("trace-2", response.getId()),
                () -> assertEquals("Entity not found", response.getMessage()),
                () -> assertEquals("User", response.getEntity()),
                () -> assertEquals(2, response.getCriteria().size())
        );
    }

    @Test
    @DisplayName("Should sort criteria by field name")
    void shouldSortCriteriaByFieldName() {
        Set<Criteria> criteria = Set.of(
                new Criteria("status", "ACTIVE"),
                new Criteria("id", "123"),
                new Criteria("email", "test@example.com")
        );

        PersistenceErrorResponse response = new PersistenceErrorResponse(
                "trace-3",
                "Error",
                "User",
                criteria
        );

        List<CriteriaResponse> list = new ArrayList<>(response.getCriteria());

        assertAll(
                () -> assertEquals("email", list.get(0).getField()),
                () -> assertEquals("id", list.get(1).getField()),
                () -> assertEquals("status", list.get(2).getField())
        );
    }

    @Test
    @DisplayName("Should sort criteria by value when fields are equal")
    void shouldSortCriteriaByValueWhenFieldsEqual() {
        Set<Criteria> criteria = Set.of(
                new Criteria("status", "INACTIVE"),
                new Criteria("status", "ACTIVE"),
                new Criteria("status", "PENDING")
        );

        PersistenceErrorResponse response = new PersistenceErrorResponse(
                "trace-4",
                "Error",
                "User",
                criteria
        );

        List<CriteriaResponse> list = new ArrayList<>(response.getCriteria());

        assertAll(
                () -> assertEquals("ACTIVE", list.get(0).getValue()),
                () -> assertEquals("INACTIVE", list.get(1).getValue()),
                () -> assertEquals("PENDING", list.get(2).getValue())
        );
    }

    @Test
    @DisplayName("Should handle null values in criteria")
    void shouldHandleNullValuesInCriteria() {
        Set<Criteria> criteria = Set.of(
                new Criteria("id", "123"),
                new Criteria("email", null)
        );

        PersistenceErrorResponse response = new PersistenceErrorResponse(
                "trace-5",
                "Error",
                "User",
                criteria
        );

        List<CriteriaResponse> list = new ArrayList<>(response.getCriteria());

        assertAll(
                () -> assertEquals("email", list.get(0).getField()),
                () -> assertNull(list.get(0).getValue()),
                () -> assertEquals("id", list.get(1).getField())
        );
    }

    @Test
    @DisplayName("Should remove duplicate criteria")
    void shouldRemoveDuplicateCriteria() {
        Set<Criteria> criteria = Set.of(
                new Criteria("id", "123")
        );

        PersistenceErrorResponse response = new PersistenceErrorResponse(
                "trace-6",
                "Error",
                "User",
                criteria
        );

        assertEquals(1, response.getCriteria().size());
    }
}
