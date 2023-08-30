package ee.bitweb.core.audit;

import ee.bitweb.core.TestSpringApplication;
import ee.bitweb.core.audit.mappers.*;
import ee.bitweb.core.audit.testcomponent.CustomAuditLogMapper;
import ee.bitweb.core.audit.testcomponent.CustomAuditLogWriter;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(
        classes = TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.audit.auto-configuration=true",
                "ee.bitweb.core.audit.mappers[0]=response_status",
                "ee.bitweb.core.audit.mappers[1]=method",
                "ee.bitweb.core.audit.blacklist[0]=/actuator/",
                "ee.bitweb.core.audit.blacklist[1]=/ignored",
                "ee.bitweb.core.audit.sensitiveHeaders[0]=authorization",
                "ee.bitweb.core.audit.sensitiveHeaders[1]=secret",
                "ee.bitweb.core.audit.includeDuration=false",
                "ee.bitweb.core.trace.auto-configuration=true",
        }
)
@ActiveProfiles({"CustomAuditLogWriter", "CustomAuditLogMapper"})
@AutoConfigureMockMvc
class AuditLogConfigurationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomAuditLogWriter writer;

    @BeforeEach
    public void beforeEachC() {
        writer.reset();
    }

    @Test
    void onMappersModifiedCorrectMappersAreApplied() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("simpleProperty", "simpleValue");
        MockHttpServletRequestBuilder mockMvcBuilder = post("/audit/validated")
                .content(payload.toString())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockMvcBuilder).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Map<String, String> container = writer.getResult();
        assertAll(
                () -> assertEquals(3, container.size()),
                () -> assertTrue(container.containsKey(CustomAuditLogMapper.KEY)),
                () -> assertTrue(container.containsKey(RequestMethodMapper.KEY)),
                () -> assertTrue(container.containsKey(ResponseStatusMapper.KEY))
        );
    }

    @Test
    void onCustomIgnoredEndpointShouldIgnoreAuditLog() throws Exception {
        MockHttpServletRequestBuilder mockMvcBuilder = get("/audit/ignored");
        mockMvc.perform(mockMvcBuilder).andDo(print())
                .andExpect(status().isOk());

        assertNull(writer.getResult());
    }
}
