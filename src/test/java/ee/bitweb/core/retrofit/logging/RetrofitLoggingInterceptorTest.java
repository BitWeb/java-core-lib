package ee.bitweb.core.retrofit.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ee.bitweb.core.TestSpringApplication;
import ee.bitweb.core.retrofit.RetrofitProperties;
import ee.bitweb.core.retrofit.builder.LoggingLevel;
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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest(
        classes = TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.trace.auto-configuration=true",
                "ee.bitweb.core.retrofit.auto-configuration=true",
                "ee.bitweb.core.retrofit.logging.level=body"
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

        logger = (Logger) LoggerFactory.getLogger("RetrofitLogger");
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

        retrofitProperties.getLogging().setSuppressedHeaders(new ArrayList<>());
        retrofitProperties.getLogging().getRedactedBodyUrls().clear();
        retrofitProperties.getLogging().setMaxLoggableRequestBodySize(1024 * 10L);
        retrofitProperties.getLogging().setMaxLoggableResponseBodySize(1024 * 10L);
    }

    @Test
    @DisplayName("Logging level BODY")
    void loggingLevelBody() throws Exception {
        retrofitProperties.getLogging().setLevel(LoggingLevel.BODY);

        executeRetrofitRequest();

        assertEquals(1, memoryAppender.getSize());

        assertLogLevel();
        assertLogMessage();

        assertAll(
                () -> assertEquals(11, memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().size()),
                () -> assertEquals("TEST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("trace_id")),
                () -> assertEquals("200", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseCode")),
                () -> assertEquals("POST", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestMethod")),
                () -> assertTrue(
                        Pattern.compile("http:\\/\\/localhost:[0-9]*\\/data-post")
                                .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestUrl")).find()
                ),
                () -> assertTrue(
                        Pattern.compile("[0-9]*")
                                .matcher(memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("Duration")).find()
                ),
                () -> assertEquals("34", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBodySize")),
                () -> assertEquals("32", memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBodySize")),
                () -> assertEquals(
                        "Content-Length: 34; Content-Type: application; X-Trace-ID: TEST",
                        memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestHeaders")
                ),
                () -> assertEquals(
                        "connection: keep-alive; Content-Length: 32; Content-Type: application/json; charset=utf-8",
                        memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseHeaders")
                ),
                () -> assertEquals(
                        "{\"message\":\"message123\",\"value\":1}",
                        memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("RequestBody")
                ),
                () -> assertEquals(
                        "{\"message\":\"message2\",\"value\":2}",
                        memoryAppender.getLoggedEvents().get(0).getMDCPropertyMap().get("ResponseBody")
                )
        );
    }

    private void assertLogLevel() {
        assertEquals(Level.INFO, memoryAppender.getLoggedEvents().get(0).getLevel());
    }

    private void assertLogMessage() {
        assertTrue(Pattern.compile(
                "POST http:\\/\\/localhost:[0-9]*\\/data-post 200 [0-9]*bytes [0-9]*ms"
        ).matcher(memoryAppender.getLoggedEvents().get(0).getFormattedMessage()).find());
    }

    private void executeRetrofitRequest() throws IOException {
        ExternalServiceApi api = builder.create(BASE_URL + server.getPort(), ExternalServiceApi.class).build();

        mockServerRequest();

        api.postData(new ExternalServiceApi.Payload("message123", 1)).execute();
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
