package ee.bitweb.core.trace.thread.decorator;

import ee.bitweb.core.trace.thread.ThreadTraceIdResolver;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class BasicMDCTaskDecoratorTest {

    @Mock
    private ThreadTraceIdResolver resolver;

    @Test
    void testMDCIsPopulatedAndCleared() {
        Mockito.when(resolver.resolve()).thenReturn(null);
        MDC.put("custom-key", "custom-value");

        new BasicMDCTaskDecorator(resolver).decorate(() -> {
            assertEquals("custom-value", MDC.get("custom-key"));
        }).run();

        assertNull(MDC.get("custom-key"));

        Mockito.verifyNoMoreInteractions(resolver);
    }

    @Test
    void testMDCIsPopulatedAndClearedWhenExceptionIsThrown() {
        Mockito.when(resolver.resolve()).thenReturn(null);
        MDC.put("custom-key", "custom-value");

        try {
            new BasicMDCTaskDecorator(resolver).decorate(() -> {
                assertEquals("custom-value", MDC.get("custom-key"));

                throw new RuntimeException();
            }).run();
        } catch (RuntimeException ignored) {
            assertNull(MDC.get("custom-key"));
        }

        Mockito.verifyNoMoreInteractions(resolver);
    }

    @Test
    void testMDCIsPopulatedGivenMDCContextMapIsNull() {
        Mockito.when(resolver.resolve()).thenReturn(null);
        assertNull(MDC.getCopyOfContextMap());

        new BasicMDCTaskDecorator(resolver).decorate(() -> {
            assertNotNull(MDC.getCopyOfContextMap());
        }).run();

        Mockito.verifyNoMoreInteractions(resolver);
    }
}
