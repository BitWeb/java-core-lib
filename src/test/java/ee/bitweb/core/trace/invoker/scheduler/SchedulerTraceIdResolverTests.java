package ee.bitweb.core.trace.invoker.scheduler;

import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class SchedulerTraceIdResolverTests {

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
        Mockito.verify(contextMock).get();
        Mockito.verify(contextMock).set("generated-mock-trace-id");
    }

    @Test
    void onTraceIdInMdcShouldThrowException() {
        // Given
        Mockito.when(contextMock.get()).thenReturn("existing-trace-id");

        // When
        Exception e = Assertions.assertThrows(
                IllegalStateException.class,
                () -> createResolver().resolve()
        );

        // Then
        Mockito.verify(contextMock).get();
        Assertions.assertEquals(
                "Context already has trace id populated, this is illegal.",
                e.getMessage()
        );
    }

    private SchedulerTraceIdResolver createResolver() {
        return new SchedulerTraceIdResolver(contextMock, creatorMock);
    }
}
