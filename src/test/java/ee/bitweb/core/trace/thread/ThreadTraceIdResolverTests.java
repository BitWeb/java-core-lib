package ee.bitweb.core.trace.thread;

import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class ThreadTraceIdResolverTests {

    @Mock
    private TraceIdContext contextMock;

    @Mock
    private TraceIdCreator creatorMock;

    @Test
    void onNoTraceIdInMdcShouldStoreTraceIdInContext() {
        // Given
        Mockito.when(creatorMock.generate(null)).thenReturn("generated-mock-trace-id");

        // When
        createResolver().resolve();

        // Then
        Mockito.verify(creatorMock).generate(null);
        Mockito.verify(contextMock, Mockito.times(2)).get();
        Mockito.verify(contextMock).set("generated-mock-trace-id");
    }

    @Test
    void onTraceIdInMdcShouldThrowException() {
        // Given
        Mockito.when(contextMock.get()).thenReturn("existing-trace-id");
        Mockito.when(creatorMock.generate("existing-trace-id")).thenReturn("existing-trace-id_new_trace_id");

        // When
        createResolver().resolve();

        // Then
        Mockito.verify(creatorMock).generate("existing-trace-id");
        Mockito.verify(contextMock, Mockito.times(2)).get();
        Mockito.verify(contextMock).set("existing-trace-id_new_trace_id");
    }

    private ThreadTraceIdResolver createResolver() {
        return new ThreadTraceIdResolver(contextMock, creatorMock);
    }
}
