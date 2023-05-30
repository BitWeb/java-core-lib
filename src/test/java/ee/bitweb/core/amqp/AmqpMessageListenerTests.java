package ee.bitweb.core.amqp;

import ee.bitweb.core.amqp.testcomponents.AmqpConfig;
import ee.bitweb.core.amqp.testcomponents.Command;
import ee.bitweb.core.amqp.testcomponents.Response;
import ee.bitweb.core.amqp.testcomponents.util.AmqpParsedMessage;
import ee.bitweb.core.amqp.testcomponents.util.AmqpTestHelper;
import ee.bitweb.core.trace.context.MDCTraceIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        void onMessagePostSendProcessorsAndListenerInterceptorsPropagateData() throws Exception {
                MDC.put(MDCTraceIdContext.DEFAULT_KEY, "some-value");
                amqpTestHelper.sendMessage(AmqpConfig.COMMAND_QUEUE_NAME, new Command(false));

                List<Message> response = amqpTestHelper.waitAndGetResponse(AmqpConfig.RESPONSE_QUEUE_NAME, 1);

                Message message = response.get(0);

                String traceId = message.getMessageProperties().getHeader("custom-header-name");
                String postProcessMessageParameter = message.getMessageProperties().getHeader("post-process-message-parameter");
                AmqpParsedMessage<Response> parsedMessage = amqpTestHelper.convert(message, Response.class);

                assertTrue(traceId.startsWith("some-value"));
                assertEquals("some-parameter-value", postProcessMessageParameter);
                assertEquals(traceId, parsedMessage.getBody().getTraceId());
                assertEquals("some-parameter-value", parsedMessage.getBody().getPostProcessMessageParameter());
        }

        @Test
        void onMessageExceptionMessageLandsToDeadLetterExchange() {
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
        void onMessageExceptionInQueueWithoutDLXMessageIsDiscarded() {
                amqpTestHelper.sendMessage(AmqpConfig.COMMAND_QUEUE_SIMPLE_NAME, new Command(true));
                amqpTestHelper.waitForEmptyQueue(AmqpConfig.COMMAND_QUEUE_SIMPLE_NAME);
        }

        @Test
        void onMessageRequeueFromDLXOnErrorShouldReturnToDLXWithCorrectHeader() {
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
