package ee.bitweb.core.trace.thread.decorator;

import ee.bitweb.core.trace.thread.ThreadTraceIdResolver;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SecurityAwareMDCTaskDecoratorTest {

    @Mock
    private ThreadTraceIdResolver resolver;

    @Test
    void testMDCIsPopulatedAndCleared() {
        Mockito.when(resolver.resolve()).thenReturn(null);
        MDC.put("custom-key", "custom-value");

        new SecurityAwareMDCTaskDecorator(resolver).decorate(() -> {
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
            new SecurityAwareMDCTaskDecorator(resolver).decorate(() -> {
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

        new SecurityAwareMDCTaskDecorator(resolver).decorate(() -> {
            assertNotNull(MDC.getCopyOfContextMap());
        }).run();

        Mockito.verifyNoMoreInteractions(resolver);
    }

    @Test
    void testDecorateCanHandleNullSecurityContext() {
        Mockito.when(resolver.resolve()).thenReturn(null);

        Runnable task;

        try (MockedStatic<SecurityContextHolder> holder = Mockito.mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(null);

            task = new SecurityAwareMDCTaskDecorator(resolver).decorate(() -> {
                assertNotNull(SecurityContextHolder.getContext());
            });
        }

        task.run();

        Mockito.verifyNoMoreInteractions(resolver);
    }
}
