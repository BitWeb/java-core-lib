package ee.bitweb.core.audit.mapper;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import ee.bitweb.core.audit.AuditLogProperties;
import ee.bitweb.core.audit.mappers.RequestForwardingDataMapper;
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
class RequestForwardingDataMapperUnitTests {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final AuditLogProperties properties = new AuditLogProperties();

    @Test
    void testAddIpAddressesUsesXForwardedForHeader() throws JacksonException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("x-forwarded-for", "192.168.69.145,192.168.69.1");

        String resultString = getMapper().getValue(request, new MockHttpServletResponse());
        Map<String, String> result = mapper.readValue(resultString, Map.class);

        assertEquals("192.168.69.145,192.168.69.1", result.get("x_forwarded_for"));
        assertEquals(1, result.size());
    }

    @Test
    void testAddIpAddressesUsesXForwardedForHeaders() throws JacksonException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("x-forwarded-for", "192.168.69.145");
        request.addHeader("x-forwarded-for", "192.168.69.1");

        String resultString = getMapper().getValue(request, new MockHttpServletResponse());
        Map<String, String> result = mapper.readValue(resultString, Map.class);


        assertEquals("192.168.69.145|192.168.69.1", result.get("x_forwarded_for"));
        assertEquals(1, result.size());
    }

    @Test
    void testAddForwardingHeadersParsesForwardedHeader() throws JacksonException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("forwarded", "for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com;secret=ruewiu");

        String resultString = getMapper().getValue(request, new MockHttpServletResponse());
        Map<String, String> result = mapper.readValue(resultString, Map.class);

        assertEquals("for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com;secret=ruewiu", result.get("forwarded"));
        assertEquals("203.0.113.60", result.get("forwarded_by"));
        assertEquals("192.0.2.43|198.51.100.17", result.get("forwarded_for"));
        assertEquals("example.com", result.get("forwarded_host"));
        assertEquals("http", result.get("forwarded_proto"));
        assertEquals("secret=ruewiu", result.get("forwarded_extensions"));
        assertEquals(6, result.size());
    }

    @Test
    void testSensitiveHeaderSettingAppliedOnForwardedHeaders() throws JacksonException {
        AuditLogProperties properties = new AuditLogProperties();
        properties.getSensitiveHeaders().add("forwarded");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("Forwarded", "for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com;secret=ruewiu");

        String resultString = new RequestForwardingDataMapper(properties, mapper).getValue(request, new MockHttpServletResponse());
        Map<String, String> result = mapper.readValue(resultString, Map.class);

        assertEquals("Len(90)", result.get("forwarded"));
        assertEquals(1, result.size());
    }

    private RequestForwardingDataMapper getMapper() {
        return new RequestForwardingDataMapper(properties, mapper);
    }
}
