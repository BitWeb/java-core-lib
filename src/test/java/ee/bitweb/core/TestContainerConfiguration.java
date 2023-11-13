package ee.bitweb.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@Configuration
public class TestContainerConfiguration {

    private static final DockerImageName RABBIT_MQ_IMAGE = DockerImageName.parse("rabbitmq:3.11-management-alpine");

    public static GenericContainer<?> rabbit;

    static {
        setupRabbit();
    }

    private static void setupRabbit() {
        rabbit = new GenericContainer<>(RABBIT_MQ_IMAGE)
                .withExposedPorts(5672)
                .withReuse(true);

        rabbit.start();

        System.setProperty("spring.rabbitmq.host", rabbit.getHost());
        System.setProperty("spring.rabbitmq.port", String.valueOf(rabbit.getFirstMappedPort()));
    }
}
