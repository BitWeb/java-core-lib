package ee.bitweb.core.retrofit.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ee.bitweb.core.TestSpringApplication;
import ee.bitweb.core.retrofit.RetrofitProperties;
import ee.bitweb.core.retrofit.builder.SpringAwareRetrofitBuilder;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.utils.MemoryAppender;
import ee.bitweb.http.server.mock.MockServer;
import io.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@SpringBootTest(
        classes = TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.trace.auto-configuration=true",
                "ee.bitweb.core.retrofit.auto-configuration=true"
        }
)
@ActiveProfiles("retrofit")
class RetrofitLoggingInterceptorTest {

    private static final String BASE_URL = "http://localhost:";

    @RegisterExtension
    private static final MockServer server = new MockServer(HttpMethod.POST, "/data-post");

    @Autowired
    private SpringAwareRetrofitBuilder builder;

    @Autowired
    private TraceIdContext context;

    @Autowired
    private RetrofitProperties retrofitProperties;

    Logger logger;
    MemoryAppender memoryAppender;

    @BeforeEach
    void beforeEach() {
        context.set("TEST");

        logger = (Logger) LoggerFactory.getLogger(RetrofitLoggingInterceptor.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @AfterEach
    void afterEach() {
        context.clear();

        memoryAppender.stop();

        retrofitProperties.getLogging().setLevel(LoggingLevel.BASIC);
        retrofitProperties.getLogging().setSuppressedHeaders(new ArrayList<>());
    }

    @Test
    @DisplayName("Logging level NONE")
    void loggingLevelNone() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.NONE);

        executeRetrofitRequest();

        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("Logging level BASIC")
    void loggingLevelBasic() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.BASIC);

        executeRetrofitRequest();

        assertEquals(2, memoryAppender.getSize());

        assertRequestLevel();
        assertRequestDescription();
        assertRequestHeaderExists(false);
        assertRequestBodyExists(false);

        assertResponseLevel();
        assertResponseDescription();
        assertResponseHeaderExists(false);
        assertResponseBodyExists(false);
    }

    @Test
    @DisplayName("Logging level HEADERS")
    void loggingLevelHeaders() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.HEADERS);

        executeRetrofitRequest();

        assertEquals(2, memoryAppender.getSize());

        assertRequestLevel();
        assertRequestDescription();
        assertRequestHeaderExists(true);
        assertRequestBodyExists(false);

        assertResponseLevel();
        assertResponseDescription();
        assertResponseHeaderExists(true);
        assertResponseBodyExists(false);
    }

    @Test
    @DisplayName("Logging level BODY")
    void loggingLevelBody() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.BODY);

        executeRetrofitRequest();

        assertEquals(2, memoryAppender.getSize());

        assertRequestLevel();
        assertRequestDescription();
        assertRequestHeaderExists(true);
        assertRequestBodyExists(true);

        assertResponseLevel();
        assertResponseDescription();
        assertResponseHeaderExists(true);
        assertResponseBodyExists(true);
    }

    @Test
    @DisplayName("Redact header")
    void redactHeader() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.HEADERS);
        retrofitProperties.getLogging().setSuppressedHeaders(List.of("content-type"));

        executeRetrofitRequest();

        assertEquals(2, memoryAppender.getSize());

        logger.info("Request: {}", memoryAppender.getLoggedEvents().get(0).getFormattedMessage());
        logger.info("Response: {}", memoryAppender.getLoggedEvents().get(1).getFormattedMessage());

        assertRequestLevel();
        assertRequestDescription();
        assertRequestHeaderExists(false);
        assertRequestBodyExists(false);
        assertRequestHeaderRedacted();

        assertResponseLevel();
        assertResponseDescription();
        assertResponseHeaderExists(false);
        assertResponseBodyExists(false);
        assertResponseHeaderRedacted();
    }

    private void assertRequestLevel() {
        assertEquals(Level.INFO, memoryAppender.getLoggedEvents().get(0).getLevel());
    }

    private void assertResponseLevel() {
        assertEquals(Level.INFO, memoryAppender.getLoggedEvents().get(1).getLevel());
    }

    private void assertRequestDescription() {
        assertTrue(Pattern.compile("--> POST http://localhost:[0-9]*/data-post")
                .matcher(memoryAppender.getLoggedEvents().get(0).getFormattedMessage()).find());
    }

    private void assertResponseDescription() {
        assertTrue(Pattern.compile("<-- 200 OK http://localhost:[0-9]*/data-post [0-9]*ms, body size 32-byte")
                .matcher(memoryAppender.getLoggedEvents().get(1).getFormattedMessage()).find());
    }

    private void assertRequestHeaderExists(boolean exists) {
        assertEquals(
                exists,
                Pattern.compile("Content-Type: application\\/json; charset=UTF-8")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getFormattedMessage()).find()
        );
    }

    private void assertResponseHeaderExists(boolean exists) {
        assertEquals(
                exists,
                Pattern.compile("Content-Type: application\\/json; charset=utf-8")
                        .matcher(memoryAppender.getLoggedEvents().get(1).getFormattedMessage()).find()
        );
    }

    private void assertRequestHeaderRedacted() {
        assertTrue(
                Pattern.compile("Content-Type:  \n")
                        .matcher(memoryAppender.getLoggedEvents().get(1).getFormattedMessage()).find()
        );
    }

    private void assertResponseHeaderRedacted() {
        assertTrue(
                Pattern.compile("Content-Type:  \n")
                        .matcher(memoryAppender.getLoggedEvents().get(1).getFormattedMessage()).find()
        );
    }

    private void assertRequestBodyExists(boolean exists) {
        assertEquals(
                exists,
                Pattern.compile("\\{\"message\":\"message1\",\"value\":1}")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getFormattedMessage()).find()
        );
    }

    private void assertResponseBodyExists(boolean exists) {
        assertEquals(
                exists,
                Pattern.compile("\\{\"message\":\"message2\",\"value\":2}")
                        .matcher(memoryAppender.getLoggedEvents().get(1).getFormattedMessage()).find()
        );
    }

    private void executeRetrofitRequest() throws IOException {
        ExternalServiceApi api = builder.create(BASE_URL + server.getPort(), ExternalServiceApi.class).build();

        mockServerRequest();

        api.postData(new ExternalServiceApi.Payload("message1", 1)).execute();
    }

    private static void mockServerRequest() {
        server.mock(
                server.requestBuilder(),
                server.responseBuilder(200, createPayload())
        );
    }

    private static JSONObject createPayload() {
        JSONObject payload = new JSONObject();

        payload.put("message", "message2");
        payload.put("value", 2);

        return payload;
    }
}