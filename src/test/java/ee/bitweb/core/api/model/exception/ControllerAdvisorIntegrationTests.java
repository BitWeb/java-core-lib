package ee.bitweb.core.api.model.exception;

import java.util.List;

import ee.bitweb.core.TestSpringApplication;
import ee.bitweb.core.api.InvalidFormatExceptionConverter;
import ee.bitweb.core.api.testcomponents.TestPingController;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.http.api.response.Criteria;
import ee.bitweb.http.api.response.ResponseAssertions;
import ee.bitweb.http.api.response.Error;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@AutoConfigureMockMvc
@ActiveProfiles("MockedInvokerTraceIdCreator")
@SpringBootTest(
        classes = TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.trace.auto-configuration=true",
                "ee.bitweb.core.controller-advice.auto-configuration=true",
                "ee.bitweb.core.controller-advice.show-detailed-field-names=false"
        }
)
class ControllerAdvisorIntegrationTests {

    private static final String TRACE_ID_HEADER_NAME = "X-Trace-ID";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void onBaseExceptionShouldReturnInternalServerError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/base-exception")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertInternalServerError(result);
        assertIdField(result);
    }

    @Test
    void onMissingRequestParamShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/with-request-param")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(result, Error.missingRequestParam("id"));
        assertIdField(result);
    }

    @Test
    void onInvalidHttpMethodShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/with-request-param")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertMethodNotAllowedError(result);
        assertIdField(result);
    }

    @Test
    void onTypeMismatchRequestParamShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/with-request-param")
                .param("id", "asd")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(result, Error.invalidTypeRequestParam("id"));
        assertIdField(result);
    }

    @Test
    void onEntityNotFoundExceptionShouldReturnNotFoundError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/not-found-exception")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertNotFoundResponse(
                result,
                "MyEntity",
                "Entity MyEntity not found",
                new Criteria("id", "SomeVal")
        );
        assertIdField(result);
    }

    @Test
    void onConflictExceptionShouldReturnConflictError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/conflict")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertConflictErrorResponse(
                result,
                "MyEntity",
                "Some conflict information",
                new Criteria("id", "SomeVal")
        );
        assertIdField(result);
    }

    @Test
    void onConstraintViolationExceptionInCodeShouldReturnBadRequestError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/constraint-violation")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertConstraintViolationErrorResponse(
                result,
                List.of(
                        Error.notBlank("simpleProperty"),
                        Error.notNull("simpleProperty")
                )
        );
        assertIdField(result);
    }

    @Test
    void onConstraintViolationExceptionShouldReturnBadRequestError() throws Exception {
        TestPingController.ComplexValidatedObject data = new TestPingController.ComplexValidatedObject();
        data.setNestedObject(new TestPingController.SimpleValidatedObject());

        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/validated")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(data));

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        Error.notBlank("complexProperty"),
                        Error.notNull("complexProperty"),
                        Error.notBlank("nestedObject.simpleProperty"),
                        Error.notNull("nestedObject.simpleProperty"),
                        Error.notEmpty("objects"),
                        Error.notNull("objects")
                )
        );
        assertIdField(result);
    }

    @Test
    void onConstraintViolationExceptionInNestedObjectShouldReturnBadRequestError() throws Exception {
        TestPingController.ComplexValidatedObject data = new TestPingController.ComplexValidatedObject();
        data.setNestedObject(new TestPingController.SimpleValidatedObject());
        data.setObjects(List.of(new TestPingController.SimpleValidatedObject()));

        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/validated")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(data));

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        Error.notBlank("complexProperty"),
                        Error.notNull("complexProperty"),
                        Error.notBlank("nestedObject.simpleProperty"),
                        Error.notNull("nestedObject.simpleProperty"),
                        Error.notBlank("objects[0].simpleProperty"),
                        Error.notNull("objects[0].simpleProperty")
                )
        );
        assertIdField(result);
    }

    @Test
    void onConstraintViolationExceptionInListObjectShouldReturnBadRequestError() throws Exception {
        TestPingController.SimpleValidatedObject firstValidSimpleObject = new TestPingController.SimpleValidatedObject();
        firstValidSimpleObject.setSimpleProperty("property");

        TestPingController.ComplexValidatedObject complexObject = new TestPingController.ComplexValidatedObject();
        complexObject.setNestedObject(new TestPingController.SimpleValidatedObject());
        complexObject.setObjects(List.of(firstValidSimpleObject, new TestPingController.SimpleValidatedObject()));

        List<TestPingController.ComplexValidatedObject> data = List.of(
                new TestPingController.ComplexValidatedObject(),
                complexObject
        );

        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/validated-list")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(data));

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertConstraintViolationErrorResponse(
                result,
                List.of(
                        Error.notBlank("complexProperty"),
                        Error.notNull("complexProperty"),
                        Error.notNull("nestedObject"),
                        Error.notEmpty("objects"),
                        Error.notNull("objects"),
                        Error.notBlank("simpleProperty"),
                        Error.notNull("simpleProperty")
                )
        );
        assertIdField(result);
    }

    @Test
    void onContentTypeMismatchShouldReturnBadRequest() throws Exception {
        TestPingController.ComplexValidatedObject data = new TestPingController.ComplexValidatedObject();
        data.setObjects(List.of(new TestPingController.SimpleValidatedObject()));

        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/validated")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.TEXT_XML_VALUE)
                .content(mapper.writeValueAsString(data));


        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertMessageNotReadableError(result);
        assertIdField(result);
    }

    @Test
    void onMethodArgumentNotValidExceptionWithinSimpleObjectInGetRequestShouldReturnBadRequestError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/validated")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        Error.notBlank("simpleProperty"),
                        Error.notNull("simpleProperty")

                )
        );
        assertIdField(result);
    }

    @Test
    void onNoContentTypeWithMultipartRequestShouldReturnBadRequestError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/import")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertContentTypeInvalidError(result);
        assertIdField(result);
    }

    @Test
    void onInvalidContentTypeWithMultipartRequestShouldReturnBadRequestError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/import")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertContentTypeInvalidError(result);
        assertIdField(result);
    }

    @Test
    void onMissingMultipartRequestPartShouldReturnBadRequestError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder =
                multipart(TestPingController.BASE_URL + "/import")
                        .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                    Error.requestPartMissing("file")
                )
        );
        assertIdField(result);
    }

    @Test
    void onInvalidEnumFieldValueShouldReturnBadRequestError() throws Exception {
        String val = "SOME INVALID ENUM VALUE";
        String reason = InvalidFormatExceptionConverter.INVALID_VALUE_REASON;
        String message = format(InvalidFormatExceptionConverter.INVALID_VALUE_MESSAGE_FORMAT,val);

        testFieldPost("enumField", "\"" + val + "\"", reason, message);
    }

    @Test
    void onInvalidBooleanFieldValueShouldReturnBadRequestError() throws Exception {
        String val = "SOME INVALID BOOLEAN VALUE";
        String reason = InvalidFormatExceptionConverter.INVALID_VALUE_REASON;
        String message = format(InvalidFormatExceptionConverter.INVALID_BOOLEAN_VALUE_MESSAGE, val);

        testFieldPost("booleanField", "\"" + val + "\"", reason, message);
    }

    @Test
    void onInvalidTemporalFieldValueShouldReturnBadRequestError() throws Exception {
        String val = "SOME INVALID DATE VALUE";
        String reason = InvalidFormatExceptionConverter.INVALID_FORMAT_REASON;
        String message = format(InvalidFormatExceptionConverter.INVALID_VALUE_MESSAGE_FORMAT, val);

        testFieldPost("localDateField", "\"" + val + "\"", reason, message);
        testFieldPost("localDateTimeField", "\"" + val + "\"", reason, message);
        testFieldPost("zonedDateTimeField", "\"" + val + "\"", reason, message);
    }

    @Test
    void onInvalidFloatFieldValueShouldReturnBadRequestError() throws Exception {
        String val = "SOME INVALID NUMERIC VALUE";
        String reason = InvalidFormatExceptionConverter.INVALID_FORMAT_REASON;
        String message = format(InvalidFormatExceptionConverter.INVALID_FLOAT_VALUE_MESSAGE, val);

        testFieldPost("doubleField", "\"" + val + "\"", reason, message);
        testFieldPost("bigDecimalField", "\"" + val + "\"", reason, message);
    }

    @Test
    void onInvalidNestedObjectFieldShouldReturnBadRequestError() throws Exception {
        String val = "whatever";

        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL)
                .content("{\"nestedTestObject\": {\"zonedDateTimeField\": \"whatever\"}}")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        Error.genericInvalidFormat("nestedTestObject.zonedDateTimeField", val)
                )
        );
        assertIdField(result);
    }

    @Test
    void onInvalidIntegerFieldValueShouldReturnBadRequest() throws Exception {
        String val = "2.9";
        String reason = InvalidFormatExceptionConverter.INVALID_FORMAT_REASON;
        String message = format(InvalidFormatExceptionConverter.INVALID_INTEGER_VALUE_MESSAGE, val);
        String messageValueUnknown = format(InvalidFormatExceptionConverter.INVALID_INTEGER_VALUE_MESSAGE, "2.9");

        testFieldPost("intField", val, reason, messageValueUnknown);
        testFieldPost("longField", val, reason, messageValueUnknown);
        testFieldPost("intField", "\"" + val + "\"", reason, message);
        testFieldPost("longField", "\"" + val + "\"", reason, message);

        val = "something_else";
        message = format(InvalidFormatExceptionConverter.INVALID_INTEGER_VALUE_MESSAGE, val);

        testFieldPost("intField", "\"" + val + "\"", reason, message);
        testFieldPost("longField", "\"" + val + "\"", reason, message);
    }

    @Test
    void onRetrofitExceptionItIsPropagated() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/retrofit-exception")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", aMapWithSize(1)))
                .andExpect(jsonPath("$.error", is("message")));
    }

    @Test
    @DisplayName("On broken pipe should return empty 503")
    void onBrokenPipe() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/broken-pipe")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @DisplayName("On generic ClientAbortException should return 500")
    void onGenericClientAbortException() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/generic-client-abort-exception")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertInternalServerError(result);
        assertIdField(result);
    }

    @Test
    void testDateFieldMethodArgumentNotValidException() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/complex-data")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON)
                .param("localDateField", "whatever")
                .accept(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        new Error("localDateField", "typeMismatch", "Unable to interpret value: whatever")
                )
        );
        assertIdField(result);
    }

    @Test
    @DisplayName("BindException: Should return validation errors for missing required form fields")
    void onBindExceptionWithMissingRequiredFieldsShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/validated-complex")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        Error.notNull("age"),
                        Error.notBlank("email"),
                        Error.notBlank("name"),
                        Error.notNull("name")
                )
        );
        assertIdField(result);
    }

    @Test
    @DisplayName("BindException: Should return validation errors for blank fields")
    void onBindExceptionWithBlankFieldsShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/validated-complex")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .param("name", "")
                .param("age", "25")
                .param("email", "  ");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        Error.notBlank("email"),
                        Error.notBlank("name")
                )
        );
        assertIdField(result);
    }

    @Test
    @DisplayName("BindException: Should return type mismatch error for invalid integer")
    void onBindExceptionWithTypeMismatchShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/validated-complex")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .param("name", "John")
                .param("age", "not-a-number")
                .param("email", "john@example.com");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        new Error("age", "typeMismatch", "Unable to interpret value: not-a-number")
                )
        );
        assertIdField(result);
    }

    @Test
    @DisplayName("BindException: Should return multiple errors for type mismatch and validation")
    void onBindExceptionWithMixedErrorsShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/validated-complex")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .param("name", "")
                .param("age", "invalid")
                .param("email", "test@example.com");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        new Error("age", "typeMismatch", "Unable to interpret value: invalid"),
                        Error.notBlank("name")
                )
        );
        assertIdField(result);
    }

    @Test
    @DisplayName("BindException: Should handle type mismatch on form binding without @Valid")
    void onPureBindExceptionWithTypeMismatchShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/form-binding")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .param("name", "John")
                .param("count", "not-a-number");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        new Error("count", "typeMismatch", "Unable to interpret value: not-a-number")
                )
        );
        assertIdField(result);
    }

    @Test
    @DisplayName("BindException: Should handle multiple type mismatches on form binding without @Valid")
    void onPureBindExceptionWithMultipleTypeMismatchesShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/form-binding")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .param("count", "abc")
                .param("date", "invalid-date");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        new Error("count", "typeMismatch", "Unable to interpret value: abc"),
                        new Error("date", "typeMismatch", "Unable to interpret value: invalid-date")
                )
        );
        assertIdField(result);
    }

    @Test
    @DisplayName("BindException: Should handle explicitly thrown BindException")
    void onExplicitBindExceptionShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/throw-bind-exception")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("INVALID_ARGUMENT")));
    }

    private void testFieldPost(String field, String value, String expectedReason, String expectedMessage) throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL)
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"" + field + "\": " + value + "}");

        ResultActions result = mockMvc.perform(mockMvcBuilder).andDo(print());
        ResponseAssertions.assertValidationErrorResponse(
                result,
                List.of(
                        new Error(field, expectedReason, expectedMessage)
                )
        );
        assertIdField(result);
    }

    private static String format(String pattern, String value) {
        return String.format(pattern, value);
    }

    private static void assertIdField(ResultActions actions) throws Exception {
        actions.andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")));
    }
}
