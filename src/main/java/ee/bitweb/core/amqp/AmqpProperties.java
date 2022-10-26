package ee.bitweb.core.amqp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "ee.bitweb.core.amqp")
public class AmqpProperties {

    private Boolean autoConfiguration = false;
}
