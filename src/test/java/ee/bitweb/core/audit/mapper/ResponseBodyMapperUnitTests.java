package ee.bitweb.core.audit.mapper;

import ee.bitweb.core.audit.AuditLogProperties;
import ee.bitweb.core.audit.mappers.ResponseBodyMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ResponseBodyMapperUnitTests {

    @Test
    void onResponseResponseBodyIsAdded() throws IOException {
        HttpServletResponse response = new MockHttpServletResponse();
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        wrapper.getWriter().write("Some content");

        String value = new ResponseBodyMapper(
                new AuditLogProperties()
        ).getValue(
                new MockHttpServletRequest(),
                wrapper
        );
        assertEquals("Some content", value);
    }

    @Test
    void onContentLargerThanMaxLoggableSizeShouldLogOnlyLength() throws IOException {
        long size = 100;
        HttpServletResponse response = new MockHttpServletResponse();
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        wrapper.getWriter().write("a".repeat((int) size));

        AuditLogProperties properties = new AuditLogProperties();
        properties.setMaxLoggableResponseSize(size - 1);

        String value = new ResponseBodyMapper(
                properties
        ).getValue(
                new MockHttpServletRequest(),
                wrapper
        );
        assertEquals("Content size: 100 characters", value);
    }

}
