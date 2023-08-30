package ee.bitweb.core.audit;

import com.google.common.net.HttpHeaders;
import ee.bitweb.core.TestSpringApplication;
import ee.bitweb.core.audit.mappers.*;
import ee.bitweb.core.audit.testcomponent.AuditLogController;
import ee.bitweb.core.audit.testcomponent.CustomAuditLogWriter;
import ee.bitweb.core.trace.invoker.http.TraceIdFilterConfig;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest(
        classes = TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.audit.auto-configuration=true",
                "ee.bitweb.core.audit.requestHeaders[0]=User-Agent",
                "ee.bitweb.core.audit.requestHeaders[1]=Origin",
                "ee.bitweb.core.audit.requestHeaders[2]=Custom-Header",
                "ee.bitweb.core.trace.auto-configuration=true"

        }
)
@ActiveProfiles("CustomAuditLogWriter")
@AutoConfigureMockMvc
class AuditLogAutoconfigurationEnabledTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private CustomAuditLogWriter writer;

        @BeforeEach
        public void init() {
                writer.reset();
        }

        @Test
        void defaultMappersIncludedAndFilterAdded() throws Exception {
                JSONObject payload = new JSONObject();
                payload.put("simpleProperty", "simpleValue");
                MockHttpServletRequestBuilder mockMvcBuilder = post(
                        AuditLogController.BASE_URL + "/validated"
                ).header(TraceIdFilterConfig.DEFAULT_HEADER_NAME, "1234567890")
                        .header(HttpHeaders.FORWARDED, "1.2.3.4")
                        .header("Custom-Header", "SomeValue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload.toString());

                mockMvc.perform(mockMvcBuilder).andDo(print())
                        .andExpect(status().isOk());

                Map<String, String> container = writer.getResult();
                assertAll(
                        () -> assertEquals(9, container.size()),
                        () -> assertTrue(container.containsKey(TraceIdMapper.KEY)),
                        () -> assertTrue(container.containsKey(AuditLogFilter.DURATION_KEY)),
                        () -> assertTrue(container.containsKey(RequestUrlDataMapper.KEY)),
                        () -> assertTrue(container.containsKey(RequestForwardingDataMapper.KEY)),
                        () -> assertTrue(container.containsKey(RequestUrlDataMapper.KEY)),
                        () -> assertTrue(container.containsKey(RequestMethodMapper.KEY)),
                        () -> assertTrue(container.containsKey(ResponseBodyMapper.KEY)),
                        () -> assertTrue(container.containsKey(ResponseStatusMapper.KEY)),
                        () -> assertTrue(container.containsKey(RequestBodyMapper.KEY))
                );
        }

        @Test
        void onActuatorEndpointShouldIgnoreAuditLog() throws Exception {
                MockHttpServletRequestBuilder mockMvcBuilder = get("/actuator/health");
                mockMvc.perform(mockMvcBuilder).andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(content().json("{\"status\":\"UP\"}"));

                assertNull(writer.getResult());
        }
}
