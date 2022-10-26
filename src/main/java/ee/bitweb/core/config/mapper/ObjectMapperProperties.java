package ee.bitweb.core.config.mapper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "ee.bitweb.core.object-mapper")
public class ObjectMapperProperties {

    private Boolean autoConfiguration = false;
}
