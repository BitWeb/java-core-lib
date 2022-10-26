package ee.bitweb.core.app.amqp.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.amqp.core.Message;

@Getter
@AllArgsConstructor
public class AmqpParsedMessage<Body> {

    private Message message;
    private Body body;
}
