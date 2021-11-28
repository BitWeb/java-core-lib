package ee.bitweb.core.trace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TraceIdTest {

    @Test
    @DisplayName("Test correct value is returned from MDC")
    void testTraceIdIsAskedFromMDC() {
        try (MockedStatic<MDC> mdc = Mockito.mockStatic(MDC.class)) {
            mdc.when(() -> MDC.get("trace_id")).thenReturn("ssas");

            assertEquals("ssas", TraceId.get());
        }
    }

    @Test
    @DisplayName("Test correct value is put to MDC")
    void testTraceIdIsPutToMDC() {
        try (MockedStatic<MDC> mdc = Mockito.mockStatic(MDC.class)) {
            TraceId.set("dsadadbh");

            mdc.verify(() -> MDC.put("trace_id", "dsadadbh"));
        }
    }
}
