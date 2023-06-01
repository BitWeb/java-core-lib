package ee.bitweb.core.audit.mapper;

import ee.bitweb.core.audit.mappers.RequestMethodMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RequestMethodMapperUnitTests {

    @Test
    void requestMethodIsExtracted() {
        HttpServletRequest request = new MockHttpServletRequest("PATCH", "/some-uri");
        HttpServletResponse response = new MockHttpServletResponse();

        Assertions.assertEquals("PATCH", new RequestMethodMapper().getValue(
                request, response
        ));
    }
}
