package ee.bitweb.core.amqp.testcomponents.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.amqp.core.Message;

@Getter
@AllArgsConstructor
public class AmqpParsedMessage<B> {

    private Message message;
    private B body;
}
