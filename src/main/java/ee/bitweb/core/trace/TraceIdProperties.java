package ee.bitweb.core.trace;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "ee.bitweb.core.trace")
public class TraceIdProperties {

    private Boolean autoConfiguration = false;
}
