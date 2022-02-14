package ee.bitweb.core.trace;

/**
 * Allows creating your own implementation for configuring TraceId. Can be used to create Properties class in Spring Boot
 * context.
 */
public interface TraceIdFormConfig {

    //String getHeaderName();

    String getPrefix();

    Character getDelimiter();

    Integer getLength();
}
