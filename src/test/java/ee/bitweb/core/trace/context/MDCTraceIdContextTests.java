package ee.bitweb.core.trace.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MDCTraceIdContextTests {

    @Test
    @DisplayName("Test correct value is returned from MDC")
    void testTraceIdIsAskedFromMDC() {
        MDCTraceIdContext context = MDCTraceIdContext.standard();

        try (MockedStatic<MDC> mdc = Mockito.mockStatic(MDC.class)) {
            mdc.when(() -> MDC.get("trace_id")).thenReturn("ssas");

            assertEquals("ssas", context.get());
        }
    }

    @Test
    @DisplayName("Test correct value is put to MDC")
    void testTraceIdIsPutToMDC() {
        MDCTraceIdContext context = MDCTraceIdContext.standard();

        try (MockedStatic<MDC> mdc = Mockito.mockStatic(MDC.class)) {
            context.set("dsadadbh");

            mdc.verify(() -> MDC.put("trace_id", "dsadadbh"));
        }
    }

    @Test
    @DisplayName("Test exception is thrown when attempting to override value in MDC")
    void testExceptionThrownWhenMDCTraceIdIsOverridden() {
        MDCTraceIdContext context = MDCTraceIdContext.standard();

        try (MockedStatic<MDC> mdc = Mockito.mockStatic(MDC.class)) {
            mdc.when(() -> MDC.get("trace_id")).thenReturn("ssas");
            Exception e = assertThrows(
                    IllegalStateException.class,
                    () -> context.set("dsadadbh")
            );

            assertEquals("Overriding existing trace id is prohibited, appending is allowed", e.getMessage());
        }
    }

    @Test
    @DisplayName("Test exception is not thrown when attempting to append value in MDC")
    void testExceptionNotThrownWhenMDCTraceIdIsAppended() {
        MDCTraceIdContext context = MDCTraceIdContext.standard();

        try (MockedStatic<MDC> mdc = Mockito.mockStatic(MDC.class)) {
            mdc.when(() -> MDC.get("trace_id")).thenReturn("ssas");
            context.set("ssas_dsadadbh");

            mdc.verify(() -> MDC.put("trace_id", "ssas_dsadadbh"));
        }
    }
}
