package ee.bitweb.core.api.model.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.TestSpringApplication;
import ee.bitweb.core.api.testcomponents.TestPingController;
import ee.bitweb.http.api.response.Error;
import ee.bitweb.http.api.response.ResponseAssertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Tag("integration")
@AutoConfigureMockMvc
@ActiveProfiles("MockedInvokerTraceIdCreator")
@SpringBootTest(
        classes = TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.trace.auto-configuration=true",
                "ee.bitweb.core.controller-advice.auto-configuration=true",
                "ee.bitweb.core.controller-advice.show-detailed-field-names=true"
        }
)
class ControllerAdvisorCompleteFileNamesIntegrationTests {

    private static final String TRACE_ID_HEADER_NAME = "X-Trace-ID";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

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
                        Error.notBlank("[0].complexProperty"),
                        Error.notNull("[0].complexProperty"),
                        Error.notNull("[0].nestedObject"),
                        Error.notEmpty("[0].objects"),
                        Error.notNull("[0].objects"),
                        Error.notBlank("[1].complexProperty"),
                        Error.notNull("[1].complexProperty"),
                        Error.notBlank("[1].nestedObject.simpleProperty"),
                        Error.notNull("[1].nestedObject.simpleProperty"),
                        Error.notBlank("[1].objects[1].simpleProperty"),
                        Error.notNull("[1].objects[1].simpleProperty")
                )
        );
        assertIdField(result);
    }

    private static void assertIdField(ResultActions actions) throws Exception {
        actions.andExpect(jsonPath("$.id", is("1234567890_generated-trace-id")));
    }
}
