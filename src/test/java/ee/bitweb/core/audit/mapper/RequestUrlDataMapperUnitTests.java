package ee.bitweb.core.audit.mapper;

import ee.bitweb.core.audit.mappers.RequestUrlDataMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.http.HttpServletResponse;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RequestUrlDataMapperUnitTests {

    @Test
    void requestUrlDataIsExtracted() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/some-uri"
        );
        request.setQueryString("someVariable=someValue1");
        HttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(302);

        Assertions.assertEquals(
                "http://localhost/some-uri?someVariable=someValue1",
                new RequestUrlDataMapper().getValue(
                        request,
                        response
                )
        );
    }
}
