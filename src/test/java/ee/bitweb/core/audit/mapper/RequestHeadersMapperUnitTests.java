package ee.bitweb.core.audit.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.audit.AuditLogProperties;
import ee.bitweb.core.audit.mappers.RequestHeadersMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RequestHeadersMapperUnitTests {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AuditLogProperties properties = new AuditLogProperties();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void originIsLoggedByDefault() throws JsonProcessingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://whatever.example");
        request.addHeader("Random", "This is unnecessary data");

        String resultString = getMapper().getValue(request, response);
        Map<String, String> result = mapper.readValue(resultString, Map.class);

        assertEquals("http://whatever.example", result.get("Origin"));
        assertEquals(1, result.size());
    }

    @Test
    void userAgentIsLoggedByDefault() throws JsonProcessingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Chrome");
        request.addHeader("Random", "This is unnecessary data");

        String resultString = getMapper().getValue(request, response);
        Map<String, String> result = mapper.readValue(resultString, Map.class);

        assertEquals("Chrome", result.get("User-Agent"));
        assertEquals(1, result.size());
    }

    @Test
    void authorizationHeaderIsSensitiveByDefault() throws JsonProcessingException {
        AuditLogProperties properties = new AuditLogProperties();
        properties.getRequestHeaders().add("Authorization");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "SecretValue");
        request.addHeader("Random", "This is unnecessary data");

        String resultString = getMapper(properties).getValue(request, response);
        Map<String, String> result = mapper.readValue(resultString, Map.class);

        assertEquals("Len(11)", result.get("Authorization"));
        assertEquals(1, result.size());
    }

    private RequestHeadersMapper getMapper() {
        return new RequestHeadersMapper(properties, mapper);
    }

    private RequestHeadersMapper getMapper(AuditLogProperties props) {
        return new RequestHeadersMapper(props, mapper);
    }
}
