package ee.bitweb.core.api.testcomponents;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ee.bitweb.core.exception.CoreException;
import ee.bitweb.core.exception.persistence.ConflictException;
import ee.bitweb.core.exception.persistence.Criteria;
import ee.bitweb.core.exception.persistence.EntityNotFoundException;
import com.fasterxml.jackson.annotation.JsonFormat;
import ee.bitweb.core.retrofit.RetrofitException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequestMapping(TestPingController.BASE_URL)
@RequiredArgsConstructor
public class TestPingController {

    public static final String LOCAL_DATE_PATTERN = "yyyy-MM-dd";
    public static final String ZONED_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String BASE_URL = "/test/ping";

    private final Validator validator;

    @PostMapping
    public ComplexData post(@RequestBody ComplexData data) {
        return data;
    }

    @PostMapping("/validated")
    public void postValidated(@Valid @RequestBody ComplexValidatedObject data) {
        log.info("{}", data);
    }

    @GetMapping("/validated")
    public void getValidated(@Valid SimpleValidatedObject data) {}

    @GetMapping("/complex-data")
    public void dateFieldParam(ComplexData data) {}

    @GetMapping("/with-request-param")
    public void get(@RequestParam("id") Long id) {}

    @PostMapping("/import")
    public void uploadFile(@RequestParam("file") MultipartFile file) {}

    @GetMapping("/base-exception")
    public void throwsBaseException() {
        throw new CoreException("CLASSIFIED_MESSAGE");
    }

    @GetMapping("/retrofit-exception")
    public void throwsRetrofitException() {
        throw new RetrofitException("Exception message", "http://example.com", HttpStatus.NOT_FOUND, "{\"error\": \"message\"}");
    }

    @GetMapping("/not-found-exception")
    public void throwsEntityNotFoundException() {
        throw new EntityNotFoundException("MyEntity", Set.of(
                new Criteria("id", "SomeVal")
        ));
    }

    @GetMapping("/conflict")
    public void throwsConflictException() {
        throw new ConflictException("Some conflict information", "MyEntity", Set.of(
                new Criteria("id", "SomeVal")
        ));
    }

    @GetMapping("/constraint-violation")
    public void throwsConstraintViolationException() {
        throw new ConstraintViolationException("Validation failed", validator.validate(new SimpleValidatedObject()));
    }

    @GetMapping("/broken-pipe")
    public void throwBrokenPipeException() throws ClientAbortException {
        throw new ClientAbortException(new IOException("Broken pipe"));
    }

    @GetMapping("/generic-client-abort-exception")
    public void throwGenericClientAbortException() throws ClientAbortException {
        throw new ClientAbortException("something wrong");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SimpleValidatedObject {

        @NotNull
        @NotBlank
        private String simpleProperty;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ComplexValidatedObject {

        @NotNull
        @NotBlank
        private String complexProperty;

        @Valid
        @NotNull
        @NotEmpty
        private List<SimpleValidatedObject> objects;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplexData {

        private String stringField;
        private Boolean booleanField;
        private Integer intField;
        private Long longField;
        private Double doubleField;
        private BigDecimal bigDecimalField;
        @JsonFormat(pattern = LOCAL_DATE_PATTERN)
        private LocalDate localDateField;
        @JsonFormat(pattern = LOCAL_DATE_PATTERN)
        private LocalDateTime localDateTimeField;
        @JsonFormat(pattern = ZONED_DATE_TIME_PATTERN)
        private ZonedDateTime zonedDateTimeField;
        private SimpleObject.EnumField enumField;
        private SimpleObject nestedTestObject;
        private List<SimpleObject> nestedTestObjectList;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleObject {

        private String stringField;
        private Boolean booleanField;
        private Long longField;
        @JsonFormat(pattern = LOCAL_DATE_PATTERN)
        private LocalDate localDateField;
        @JsonFormat(pattern = LOCAL_DATE_PATTERN)
        private LocalDateTime localDateTimeField;
        @JsonFormat(pattern = ZONED_DATE_TIME_PATTERN)
        private ZonedDateTime zonedDateTimeField;
        private EnumField enumField = EnumField.VALUE_2;

        private enum EnumField {
            VALUE_1,
            VALUE_2,
        }
    }
}
