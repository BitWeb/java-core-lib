package ee.bitweb.core.object_mapper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static ee.bitweb.core.object_mapper.ObjectMapperProperties.PREFIX;

@Setter
@Getter
@Component
@ConfigurationProperties(PREFIX)
@ConditionalOnProperty(value = PREFIX + ".auto-configuration", havingValue = "true")
public class ObjectMapperProperties {

    static final String PREFIX = "ee.bitweb.core.object-mapper";

    private Boolean autoConfiguration = false;
}
