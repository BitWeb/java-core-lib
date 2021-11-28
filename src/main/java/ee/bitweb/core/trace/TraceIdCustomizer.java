package ee.bitweb.core.trace;

import java.util.List;

/**
 * Allows creating your own implementation for configuring TraceId. Can be used to create Properties class in Spring Boot
 * context.
 */
public interface TraceIdCustomizer {

    String getHeaderName();

    String getPrefix();

    char getDelimiter();

    int getLength();

    List<AdditionalHeader> getAdditionalHeaders();
}
