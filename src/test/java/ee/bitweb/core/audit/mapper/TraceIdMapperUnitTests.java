package ee.bitweb.core.audit.mapper;

import ee.bitweb.core.audit.mappers.TraceIdMapper;
import ee.bitweb.core.trace.context.TraceIdContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TraceIdMapperUnitTests {

    @Test
    void traceIdShouldBeExtractedThroughContext() {

        Assertions.assertEquals(
                "perfectly-fine-trace-id",
                new TraceIdMapper(
                        new DummyTraceIdContext()
                ).getValue(
                        new MockHttpServletRequest(),
                        new MockHttpServletResponse()
                )
        );
    }

    static class DummyTraceIdContext implements TraceIdContext {

        @Override
        public void set(String traceId) {
            throw new IllegalCallerException("Should not be called");
        }

        @Override
        public String get() {
            return "perfectly-fine-trace-id";
        }

        @Override
        public void clear() {
            throw new IllegalCallerException("Should not be called");
        }
    }
}
