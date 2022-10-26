package ee.bitweb.core.amqp;

import ee.bitweb.core.app.amqp.AmqpConfig;
import ee.bitweb.core.app.amqp.Command;
import ee.bitweb.core.app.amqp.Response;
import ee.bitweb.core.trace.context.MDCTraceIdContext;
import ee.bitweb.core.app.amqp.util.AmqpParsedMessage;
import ee.bitweb.core.app.amqp.util.AmqpTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = {
                "ee.bitweb.core.trace.auto-configuration=true",
                "ee.bitweb.core.amqp.auto-configuration=true",
                "ee.bitweb.core.trace.invoker.amqp.header-name=custom-header-name",
                "test.listener.enabled=true"
        }
)
@ActiveProfiles("AmqpTest")
class AmqpMessageListenerTests {

        @Autowired
        private AmqpTestHelper amqpTestHelper;

        @AfterEach
        public void cleanup() {
                amqpTestHelper.clear(AmqpConfig.COMMAND_QUEUE_NAME);
                amqpTestHelper.clear(AmqpConfig.COMMAND_QUEUE_SIMPLE_NAME);
                amqpTestHelper.clear(AmqpConfig.COMMAND_DEAD_LETTER_EXCHANGE_NAME);
                amqpTestHelper.clear(AmqpConfig.RESPONSE_QUEUE_NAME);
                MDC.clear();
        }

        @Test
        void traceIdPropagates() throws Exception {
                MDC.put(MDCTraceIdContext.DEFAULT_KEY, "some-value");
                amqpTestHelper.sendMessage(AmqpConfig.COMMAND_QUEUE_NAME, new Command(false));

                List<Message> response = amqpTestHelper.waitAndGetResponse(AmqpConfig.RESPONSE_QUEUE_NAME, 1);

                Message message = response.get(0);

                String traceId = message.getMessageProperties().getHeader("custom-header-name");
                AmqpParsedMessage<Response> parsedMessage = amqpTestHelper.convert(message, Response.class);

                assertTrue(traceId.startsWith("some-value"));
                assertEquals(traceId, parsedMessage.getBody().getTraceId());
        }

        @Test
        void onMessageExceptionMessageLandsToDeadLetterExchange() throws Exception {
                MDC.put(MDCTraceIdContext.DEFAULT_KEY, "some-value");
                amqpTestHelper.sendMessage(AmqpConfig.COMMAND_QUEUE_NAME, new Command(true));

                List<Message> response = amqpTestHelper.waitAndGetResponse(AmqpConfig.COMMAND_DEAD_LETTER_EXCHANGE_NAME, 1);
                Message message = response.get(0);

                List<Map<String, ?>> xDeath = message.getMessageProperties().getXDeathHeader();
                Map<String, ?> xDeathObject = xDeath.get(0);

                assertTrue(xDeathObject.containsKey("count"));
                assertEquals(1L, xDeathObject.get("count"));
        }

        @Test
        void onMessageExceptionInQueueWithoutDLXMessageIsDiscarded() throws Exception {
                amqpTestHelper.sendMessage(AmqpConfig.COMMAND_QUEUE_SIMPLE_NAME, new Command(true));
                amqpTestHelper.waitForEmptyQueue(AmqpConfig.COMMAND_QUEUE_SIMPLE_NAME);
        }

        @Test
        void onMessageRequeueFromDLXOnErrorShouldReturnToDLXWithCorrectHeader() throws Exception {
                MDC.put(MDCTraceIdContext.DEFAULT_KEY, "some-value");
                amqpTestHelper.sendMessage(AmqpConfig.COMMAND_QUEUE_NAME, new Command(true));

                List<Message> response = amqpTestHelper.waitAndGetResponse(AmqpConfig.COMMAND_DEAD_LETTER_EXCHANGE_NAME, 1);
                amqpTestHelper.sendMessage(AmqpConfig.COMMAND_QUEUE_NAME, response.get(0));

                response = amqpTestHelper.waitAndGetResponse(AmqpConfig.COMMAND_DEAD_LETTER_EXCHANGE_NAME, 1);
                Message message = response.get(0);

                List<Map<String, ?>> xDeath = message.getMessageProperties().getXDeathHeader();
                Map<String, ?> xDeathObject = xDeath.get(0);

                assertTrue(xDeathObject.containsKey("count"));
                assertEquals(2L, xDeathObject.get("count"));
        }
}
