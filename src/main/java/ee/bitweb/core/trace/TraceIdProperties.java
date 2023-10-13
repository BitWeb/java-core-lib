package ee.bitweb.core.trace;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static ee.bitweb.core.trace.TraceIdProperties.PREFIX;

@Setter
@Getter
@Component
@ConfigurationProperties(PREFIX)
public class TraceIdProperties {

    static final String PREFIX = "ee.bitweb.core.trace";

    private Boolean autoConfiguration = false;
}
