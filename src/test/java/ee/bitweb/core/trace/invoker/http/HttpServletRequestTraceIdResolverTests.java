package ee.bitweb.core.trace.invoker.http;

import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class HttpServletRequestTraceIdResolverTests {

    @Mock
    TraceIdContext contextMock;

    @Mock
    TraceIdCreator creatorMock;

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("Default configuration, header is not present in request, must return new trace id")
    void testCorrectTraceIdIsGeneratedWhenNonePresent() {
        // Given
        Mockito.when(request.getHeader("X-Trace-ID")).thenReturn(null);
        Mockito.when(creatorMock.generate(null)).thenReturn("generated-mock-trace-id");

        // When
        createResolver().resolve(request);

        // Then
        Mockito.verify(creatorMock).generate(null);
        Mockito.verify(contextMock).set("generated-mock-trace-id");
    }

    @Test
    @DisplayName("Default configuration, header is not present in request, must return new trace id")
    void testCorrectTraceIdIsGeneratedWhenPresent() {
        // Given
        Mockito.when(request.getHeader("X-Trace-ID")).thenReturn("incoming-trace-id");
        Mockito.when(creatorMock.generate("incoming-trace-id")).thenReturn("incoming-trace-id_generated-mock-trace-id");

        // When
        createResolver().resolve(request);

        // Then
        Mockito.verify(creatorMock).generate("incoming-trace-id");
        Mockito.verify(contextMock).set("incoming-trace-id_generated-mock-trace-id");
    }

    private HttpServletRequestTraceIdResolver createResolver() {
        return new HttpServletRequestTraceIdResolver(new TraceIdFilterConfig().getHeaderName(), contextMock, creatorMock);
    }
}
