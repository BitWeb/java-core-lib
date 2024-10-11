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
        context.clear();
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
        retrofitProperties.getLogging().getRedactedBodyUrls().clear();
        retrofitProperties.getLogging().setMaxLoggableRequestBodySize(1024 * 10L);
        retrofitProperties.getLogging().setMaxLoggableResponseBodySize(1024 * 10L);
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

        assertEquals(1, memoryAppender.getSize());

        assertLogLevel();
        assertLogMessage();

        assertEquals(9, memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().size());
        assertEquals("TEST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("trace_id"));
        assertEquals("200", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseCode"));
        assertEquals("POST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestMethod"));
        assertTrue(
                Pattern.compile("http:\\/\\/localhost:[0-9]*\\/data-post")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestUrl")).find()
        );
        assertEquals("-", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestProtocol"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestDuration")).find()
        );
        assertEquals("OK", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseMessage"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBodySize")).find()
        );
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBodySize")).find()
        );
    }

    @Test
    @DisplayName("Logging level HEADERS")
    void loggingLevelHeaders() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.HEADERS);

        executeRetrofitRequest();

        assertEquals(1, memoryAppender.getSize());

        assertLogLevel();
        assertLogMessage();

        assertEquals(11, memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().size());
        assertEquals("TEST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("trace_id"));
        assertEquals("200", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseCode"));
        assertEquals("POST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestMethod"));
        assertTrue(
                Pattern.compile("http:\\/\\/localhost:[0-9]*\\/data-post")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestUrl")).find()
        );
        assertEquals("-", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestProtocol"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestDuration")).find()
        );
        assertEquals("OK", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseMessage"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBodySize")).find()
        );
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBodySize")).find()
        );
        assertEquals(
                "Content-Length: 32; Content-Type: application/json; charset=UTF-8",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestHeaders")
        );
        assertEquals(
                "connection: keep-alive; Content-Length: 32; Content-Type: application/json; charset=utf-8",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseHeaders")
        );
    }

    @Test
    @DisplayName("Logging level BODY")
    void loggingLevelBody() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.BODY);

        executeRetrofitRequest();

        assertEquals(1, memoryAppender.getSize());

        assertLogLevel();
        assertLogMessage();

        assertEquals(13, memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().size());
        assertEquals("TEST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("trace_id"));
        assertEquals("200", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseCode"));
        assertEquals("POST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestMethod"));
        assertTrue(
                Pattern.compile("http:\\/\\/localhost:[0-9]*\\/data-post")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestUrl")).find()
        );
        assertEquals("-", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestProtocol"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestDuration")).find()
        );
        assertEquals("OK", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseMessage"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBodySize")).find()
        );
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBodySize")).find()
        );
        assertEquals(
                "Content-Length: 32; Content-Type: application/json; charset=UTF-8",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestHeaders")
        );
        assertEquals(
                "connection: keep-alive; Content-Length: 32; Content-Type: application/json; charset=utf-8",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseHeaders")
        );
        assertEquals(
                "{\"message\":\"message1\",\"value\":1}",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBody")
        );
        assertEquals(
                "{\"message\":\"message2\",\"value\":2}",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBody")
        );
    }

    @Test
    @DisplayName("Redact header")
    void redactHeader() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.HEADERS);
        retrofitProperties.getLogging().setSuppressedHeaders(List.of("content-type"));

        executeRetrofitRequest();

        assertEquals(1, memoryAppender.getSize());

        assertLogLevel();
        assertLogMessage();

        assertEquals(11, memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().size());
        assertEquals("TEST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("trace_id"));
        assertEquals("200", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseCode"));
        assertEquals("POST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestMethod"));
        assertTrue(
                Pattern.compile("http:\\/\\/localhost:[0-9]*\\/data-post")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestUrl")).find()
        );
        assertEquals("-", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestProtocol"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestDuration")).find()
        );
        assertEquals("OK", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseMessage"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBodySize")).find()
        );
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBodySize")).find()
        );
        assertEquals(
                "Content-Length: 32; Content-Type:  ",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestHeaders")
        );
        assertEquals(
                "connection: keep-alive; Content-Length: 32; Content-Type:  ",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseHeaders")
        );
    }

    @Test
    @DisplayName("Redact body url")
    void redactBodyUrl() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.BODY);
        retrofitProperties.getLogging().getRedactedBodyUrls().add(BASE_URL + server.getPort() + "/data-post");

        executeRetrofitRequest();

        assertEquals(1, memoryAppender.getSize());

        assertLogLevel();
        assertLogMessage();

        assertEquals(13, memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().size());
        assertEquals("TEST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("trace_id"));
        assertEquals("200", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseCode"));
        assertEquals("POST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestMethod"));
        assertTrue(
                Pattern.compile("http:\\/\\/localhost:[0-9]*\\/data-post")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestUrl")).find()
        );
        assertEquals("-", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestProtocol"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestDuration")).find()
        );
        assertEquals("OK", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseMessage"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBodySize")).find()
        );
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBodySize")).find()
        );
        assertEquals(
                "Content-Length: 32; Content-Type: application/json; charset=UTF-8",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestHeaders")
        );
        assertEquals(
                "connection: keep-alive; Content-Length: 32; Content-Type: application/json; charset=utf-8",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseHeaders")
        );
        assertEquals(
                "(body redacted)",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBody")
        );
        assertEquals(
                "(body redacted)",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBody")
        );
    }

    @Test
    @DisplayName("Limit logged body size")
    void limitLoggedBodySize() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.BODY);
        retrofitProperties.getLogging().setMaxLoggableRequestBodySize(5L);
        retrofitProperties.getLogging().setMaxLoggableResponseBodySize(10L);

        executeRetrofitRequest();

        assertEquals(1, memoryAppender.getSize());

        assertLogLevel();
        assertLogMessage();

        assertEquals(13, memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().size());
        assertEquals("TEST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("trace_id"));
        assertEquals("200", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseCode"));
        assertEquals("POST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestMethod"));
        assertTrue(
                Pattern.compile("http:\\/\\/localhost:[0-9]*\\/data-post")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestUrl")).find()
        );
        assertEquals("-", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestProtocol"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestDuration")).find()
        );
        assertEquals("OK", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseMessage"));
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBodySize")).find()
        );
        assertTrue(
                Pattern.compile("[0-9]*")
                        .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBodySize")).find()
        );
        assertEquals(
                "Content-Length: 32; Content-Type: application/json; charset=UTF-8",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestHeaders")
        );
        assertEquals(
                "connection: keep-alive; Content-Length: 32; Content-Type: application/json; charset=utf-8",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseHeaders")
        );
        assertEquals(
                "{\"mes ... Content size: 32 characters",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBody")
        );
        assertEquals(
                "{\"message\" ... Content size: 32 characters",
                memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBody")
        );
    }

    private void assertLogLevel() {
        assertEquals(Level.INFO, memoryAppender.getLoggedEvents().get(0).getLevel());
    }

    private void assertLogMessage() {
        logger.info(memoryAppender.getLoggedEvents().get(0).getFormattedMessage());
        assertTrue(Pattern.compile(
                "Method\\(POST\\), URL\\(http:\\/\\/localhost:[0-9]*\\/data-post\\) Status\\(200\\) ResponseSize\\([0-9]*\\) Duration\\([0-9]* ms\\)"
        ).matcher(memoryAppender.getLoggedEvents().get(0).getFormattedMessage()).find());
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