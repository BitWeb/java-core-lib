package ee.bitweb.core.amqp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static ee.bitweb.core.amqp.AmqpProperties.PREFIX;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = PREFIX)
@ConditionalOnProperty(value = AmqpProperties.PREFIX + ".auto-configuration", havingValue = "true")
public class AmqpProperties {

    static final String PREFIX = "ee.bitweb.core.amqp";

    private Boolean autoConfiguration = false;
}
