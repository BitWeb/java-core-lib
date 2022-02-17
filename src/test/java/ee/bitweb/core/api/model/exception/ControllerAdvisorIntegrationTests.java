package ee.bitweb.core.api.model.exception;

import java.util.List;

import ee.bitweb.core.TestSpringApplication;
import ee.bitweb.core.api.InvalidFormatExceptionConverter;
import ee.bitweb.core.app.TestPingController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
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

@AutoConfigureMockMvc
@ActiveProfiles("MockedInvokerTraceIdCreator")
@SpringBootTest(
        classes= TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.trace.auto-configuration=true",
                "ee.bitweb.core.controller-advice.enabled=true"
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

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("INTERNAL_SERVER_ERROR")));
    }

    @Test
    void onMissingRequestParamShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/with-request-param")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(3)))
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("INVALID_ARGUMENT")))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]", aMapWithSize(3)))
                .andExpect(jsonPath("$.errors[0].field", is("id")))
                .andExpect(jsonPath("$.errors[0].reason", is("MissingValue")))
                .andExpect(jsonPath("$.errors[0].message", is("Request parameter is required")));
    }

    @Test
    void onInvalidHttpMethodShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/with-request-param")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("METHOD_NOT_ALLOWED")));
    }

    @Test
    void onTypeMismatchRequestParamShouldReturnBadRequest() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/with-request-param")
                .param("id", "asd")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(3)))
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("INVALID_ARGUMENT")))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]", aMapWithSize(3)))
                .andExpect(jsonPath("$.errors[0].field", is("id")))
                .andExpect(jsonPath("$.errors[0].reason", is("InvalidType")))
                .andExpect(jsonPath("$.errors[0].message", is("Request parameter is invalid")));
    }

    @Test
    void onEntityNotFoundExceptionShouldReturnNotFoundError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/not-found-exception")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", aMapWithSize(4)))
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("Entity MyEntity not found")))
                .andExpect(jsonPath("$.entity", is("MyEntity")))
                .andExpect(jsonPath("$.criteria", hasSize(1)))
                .andExpect(jsonPath("$.criteria[0]", aMapWithSize(2)))
                .andExpect(jsonPath("$.criteria[0].field", is("id")))
                .andExpect(jsonPath("$.criteria[0].value", is("SomeVal")));
    }

    @Test
    void onConflictExceptionShouldReturnConflictError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/conflict")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$", aMapWithSize(4)))
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("Some conflict information")))
                .andExpect(jsonPath("$.entity", is("MyEntity")))
                .andExpect(jsonPath("$.criteria", hasSize(1)))
                .andExpect(jsonPath("$.criteria[0]", aMapWithSize(2)))
                .andExpect(jsonPath("$.criteria[0].field", is("id")))
                .andExpect(jsonPath("$.criteria[0].value", is("SomeVal")));
    }

    @Test
    void onConstraintViolationExceptionInCodeShoudReturnBadRequestError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/constraint-violation")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        MvcResult result = mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        JSONAssert.assertEquals(
                getValidationErrorResponse(
                        "CONSTRAINT_VIOLATION",
                        List.of(
                                fieldError("simpleProperty", "NotBlank", "must not be blank"),
                                fieldError("simpleProperty", "NotNull", "must not be null")
                        )
                ),
                new JSONObject(result.getResponse().getContentAsString()),
                JSONCompareMode.NON_EXTENSIBLE
        );
    }

    @Test
    void onConstraintViolationExceptionShouldReturnBadRequestError() throws Exception {
        TestPingController.ComplexValidatedObject data = new TestPingController.ComplexValidatedObject();

        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/validated")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(data));

        MvcResult result = mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        JSONAssert.assertEquals(
                getValidationErrorResponse(
                        "INVALID_ARGUMENT",
                        List.of(
                                fieldError("complexProperty", "NotNull", "must not be null"),
                                fieldError("complexProperty", "NotBlank", "must not be blank"),
                                fieldError("objects", "NotNull", "must not be null"),
                                fieldError("objects", "NotEmpty", "must not be empty")
                        )
                ),
                new JSONObject(result.getResponse().getContentAsString()),
                JSONCompareMode.NON_EXTENSIBLE
        );
    }

    @Test
    void onConstraintViolationExceptionInNestedObjectShouldReturnBadRequestError() throws Exception {
        TestPingController.ComplexValidatedObject data = new TestPingController.ComplexValidatedObject();
        data.setObjects(List.of(new TestPingController.SimpleValidatedObject()));

        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/validated")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(data));

        MvcResult result = mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        JSONAssert.assertEquals(
                getValidationErrorResponse(
                        "INVALID_ARGUMENT",
                        List.of(
                                fieldError("complexProperty", "NotNull", "must not be null"),
                                fieldError("complexProperty", "NotBlank", "must not be blank"),
                                fieldError("objects[0].simpleProperty", "NotNull", "must not be null"),
                                fieldError("objects[0].simpleProperty", "NotBlank", "must not be blank")
                        )
                ),
                new JSONObject(result.getResponse().getContentAsString()),
                JSONCompareMode.NON_EXTENSIBLE
        );
    }

    @Test
    void onContentTypeMismatchShouldReturnBadRequest() throws Exception {
        TestPingController.ComplexValidatedObject data = new TestPingController.ComplexValidatedObject();
        data.setObjects(List.of(new TestPingController.SimpleValidatedObject()));

        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/validated")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.TEXT_XML_VALUE)
                .content(mapper.writeValueAsString(data));

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("MESSAGE_NOT_READABLE")));
    }

    @Test
    void onConstraintViolationExceptionWithinNestedObjectInGetRequestShouldReturnBadRequestError() throws Exception {
        TestPingController.ComplexValidatedObject data = new TestPingController.ComplexValidatedObject();

        MockHttpServletRequestBuilder mockMvcBuilder = get(TestPingController.BASE_URL + "/validated")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .content(mapper.writeValueAsString(data));

        MvcResult result = mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        JSONAssert.assertEquals(
                getValidationErrorResponse("INVALID_ARGUMENT",
                        List.of(
                                fieldError("simpleProperty", "NotNull", "must not be null"),
                                fieldError("simpleProperty", "NotBlank", "must not be blank")
                        )
                ),
                new JSONObject(result.getResponse().getContentAsString()),
                JSONCompareMode.NON_EXTENSIBLE
        );
    }

    @Test
    void onNoContentTypeWithMultipartRequestShouldReturnBadRequestError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/import")
                .header(TRACE_ID_HEADER_NAME, "1234567890");

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("CONTENT_TYPE_NOT_VALID")));
    }

    @Test
    void onInvalidContentTypeWithMultipartRequestShouldReturnBadRequestError() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL + "/import")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.message", is("CONTENT_TYPE_NOT_VALID")));
    }

    @Test
    void onMissingMultipartRequestPartShouldReturnBadRequestError () throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder =
                multipart(TestPingController.BASE_URL + "/import")
                        .header(TRACE_ID_HEADER_NAME, "1234567890");

        MvcResult result = mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        JSONAssert.assertEquals(
                getValidationErrorResponse("INVALID_ARGUMENT",
                        List.of(
                                fieldError("file", "RequestPartPresent", "Required request part 'file' is not present")
                        )
                ),
                new JSONObject(result.getResponse().getContentAsString()),
                JSONCompareMode.NON_EXTENSIBLE
        );
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
        String reason = InvalidFormatExceptionConverter.INVALID_FORMAT_REASON;
        String message = format(InvalidFormatExceptionConverter.INVALID_VALUE_MESSAGE_FORMAT, val);

        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL)
                .content("{\"nestedTestObject\": {\"zonedDateTimeField\": \"whatever\"}}")
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("INVALID_ARGUMENT")))
                .andExpect(jsonPath("$.errors[0].field", is("nestedTestObject.zonedDateTimeField")))
                .andExpect(jsonPath("$.errors[0].reason", is(reason)))
                .andExpect(jsonPath("$.errors[0].message", is(message)));
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

    private void testFieldPost(String field, String value, String expectedReason, String expectedMessage) throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = post(TestPingController.BASE_URL)
                .header(TRACE_ID_HEADER_NAME, "1234567890")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"" + field + "\": " + value + "}");

        mockMvc.perform(mockMvcBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(3)))
                .andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")))
                .andExpect(jsonPath("$.message", is("INVALID_ARGUMENT")))
                .andExpect(jsonPath("$.errors[0]", aMapWithSize(3)))
                .andExpect(jsonPath("$.errors[0].field", is(field)))
                .andExpect(jsonPath("$.errors[0].reason", is(expectedReason)))
                .andExpect(jsonPath("$.errors[0].message", is(expectedMessage)));
    }

    private static String format(String pattern, String value) {
        return String.format(pattern, value);
    }

    private JSONObject getValidationErrorResponse(String message, List<JSONObject> errors) {
        JSONObject response = new JSONObject();
        JSONArray jsonErrors = new JSONArray(errors);
        response.put("id", "1234567890_generated-trace-id");
        response.put("message", message);
        response.put("errors", jsonErrors);

        return response;
    }

    private JSONObject fieldError(String field, String reason, String message) {
        JSONObject fieldError = new JSONObject();
        fieldError.put("field", field);
        fieldError.put("reason", reason);
        fieldError.put("message", message);

        return fieldError;
    }
}
