package ee.bitweb.core.audit.mapper;

import ee.bitweb.core.audit.mappers.ResponseStatusMapper;
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
class ResponseStatusMapperUnitTests {

    @Test
    void responseStatusIsExtracted() {
        HttpServletRequest request = new MockHttpServletRequest("PATCH", "/some-uri");
        HttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(302);

        Assertions.assertEquals("302", new ResponseStatusMapper().getValue(
                request,
                response
        ));
    }
}
