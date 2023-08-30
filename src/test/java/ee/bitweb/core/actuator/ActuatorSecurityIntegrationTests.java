package ee.bitweb.core.actuator;

import ee.bitweb.core.TestSpringApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@AutoConfigureMockMvc
@ActiveProfiles("MockedInvokerTraceIdCreator")
@SpringBootTest(
        classes= TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.actuator.security.enabled=true",
                "ee.bitweb.core.actuator.security.user.roles=ACTUATOR",
                "management.endpoints.web.exposure.include=*",
                "management.endpoint.health.probes.enabled=true",
                "management.endpoint.health.show-details=when-authorized",
        }
)
class ActuatorSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActuatorSecurityProperties securityProperties;

    @Autowired
    private WebEndpointProperties webEndpointProperties;

    @Test
    @DisplayName("No authentication provided, must return 401")
    void testWithoutAuthenticationReturns401() throws Exception {
        MockHttpServletRequestBuilder request = buildRequest();

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Wrong username provided, must return 401")
    void testWrongUsernameReturns401() throws Exception {
        MockHttpServletRequestBuilder request = buildRequest()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader("invalid", ""));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Wrong password provided, must return 401")
    void testWrongPasswordReturns401() throws Exception {
        MockHttpServletRequestBuilder request = buildRequest()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(securityProperties.getUser().getName(), ""));

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Correct username and password, must return 200")
    void testCorrectUsernameAndPasswordReturn200() throws Exception {
        MockHttpServletRequestBuilder request = buildRequest()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader());

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("No authentication provided, must return 200 for health endpoint")
    void testWithoutCredentialsHealthReturns200() throws Exception {
        MockHttpServletRequestBuilder request = buildRequest("/health");

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.groups", hasSize(2)))
                .andExpect(jsonPath("$.groups[0]", is("liveness")))
                .andExpect(jsonPath("$.groups[1]", is("readiness")));
    }

    @Test
    @DisplayName("Authentication provided, must return 200 for health endpoint")
    void testWithCredentialsHealthReturns200() throws Exception {
        MockHttpServletRequestBuilder request = buildRequest("/health")
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader());

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.groups", hasSize(2)))
                .andExpect(jsonPath("$.groups[0]", is("liveness")))
                .andExpect(jsonPath("$.groups[1]", is("readiness")));
    }

    private MockHttpServletRequestBuilder buildRequest() {
        return buildRequest(null);
    }

    private MockHttpServletRequestBuilder buildRequest(String path) {
        String basePath = webEndpointProperties.getBasePath();
        if (path != null) {
            basePath += path;
        }

        return get(basePath).accept(MediaType.APPLICATION_JSON);
    }

    private String getBasicAuthHeader() {
        var userConfig = securityProperties.getUser();

        return getBasicAuthHeader(userConfig.getName(), userConfig.getPassword());
    }

    private String getBasicAuthHeader(String user, String password) {
        return "Basic " + Base64Utils.encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
