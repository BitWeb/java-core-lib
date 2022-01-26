package ee.bitweb.core.app;

import java.util.Collections;
import java.util.List;

import ee.bitweb.core.trace.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TraceIdFilterConfig {

    @Bean
    public TraceIdFilter traceIdFilter() {
        return new TraceIdFilter(new IdentityCustomizer());
    }

    public static class IdentityCustomizer implements TraceIdCustomizer {

        @Override
        public String getHeaderName() {
            return "X-Trace-ID";
        }

        @Override
        public String getPrefix() {
            return null;
        }

        @Override
        public char getDelimiter() {
            return '_';
        }

        @Override
        public int getLength() {
            return 0; // Otherwise it is impossible to perform JSONAssert.assertEquals of the error response.
        }

        @Override
        public List<AdditionalHeader> getAdditionalHeaders() {
            return Collections.emptyList();
        }
    }

}
