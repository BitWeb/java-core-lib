package ee.bitweb.core.retrofit.builder;

import ee.bitweb.core.TestSpringApplication;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.retrofit.helpers.RequestCountInterceptor;
import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.invoker.http.TraceIdFilterConfig;
import ee.bitweb.http.server.mock.MockServer;
import io.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@Tag("integration")
@SpringBootTest(
        classes= TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.trace.auto-configuration=true",
                "ee.bitweb.core.retrofit.auto-configuration=true"
        }
)
@ActiveProfiles("retrofit")
class SpringAwareRetrofitBuilderTests {

    private static final String BASE_URL = "http://localhost:";

    @RegisterExtension
    private static final MockServer server = new MockServer(HttpMethod.GET, "/request");



    @Autowired
    private SpringAwareRetrofitBuilder builder;

    @Autowired
    private TraceIdContext context;

    @Autowired
    private TraceIdFilterConfig config;

    @Autowired
    @Qualifier("interceptor1")
    private RequestCountInterceptor interceptor1;

    @Autowired
    @Qualifier("interceptor2")
    private RequestCountInterceptor interceptor2;

    @BeforeEach
    void reset() {
        context.clear();
        interceptor1.reset();
        interceptor2.reset();
    }

    @Test
    void byDefaultTraceIdInterceptorIsAdded() throws Exception {
        context.set("some-trace-id-value");
        mockServerGet(
                List.of(
                    new Header(config.getHeaderName(), "some-trace-id-value")
                ),
                "message",
                1
        );
        ExternalServiceApi api = builder.create(BASE_URL  + server.getPort(), ExternalServiceApi.class).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue())
        );
    }

    @Test
    void interceptorBeansAreIncludedInApi() throws Exception {
        context.set("some-trace-id-value");
        mockServerGet(
                List.of(
                        new Header(config.getHeaderName(), "some-trace-id-value")
                ),
                "message",
                1
        );
        ExternalServiceApi api = builder.create(BASE_URL  + server.getPort(), ExternalServiceApi.class).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue()),
                () -> Assertions.assertEquals(1, interceptor1.getCount()),
                () -> Assertions.assertEquals(1, interceptor2.getCount())
        );
    }

    @Test
    void defaultInterceptorsAreRemovable()  throws Exception {
        context.set("some-trace-id-value");
        mockServerGet(
                List.of(
                        new Header(config.getHeaderName(), "some-trace-id-value")
                ),
                "message",
                1
        );
        ExternalServiceApi api = builder
                .create(BASE_URL  + server.getPort(), ExternalServiceApi.class)
                .remove(interceptor2).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue()),
                () -> Assertions.assertEquals(1, interceptor1.getCount()),
                () -> Assertions.assertEquals(0, interceptor2.getCount())
        );
    }

    @Test
    void addedCustomInterceptorIsAppliedToApi() throws Exception {
        context.set("some-trace-id-value");
        mockServerGet(
                List.of(
                        new Header(config.getHeaderName(), "some-trace-id-value")
                ),
                "message",
                1
        );
        RequestCountInterceptor customInterceptor = new RequestCountInterceptor();

        ExternalServiceApi api = builder.create(
                BASE_URL  + server.getPort(),
                ExternalServiceApi.class
        ).add(
                customInterceptor
        ).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue()),
                () -> Assertions.assertEquals(1, interceptor1.getCount()),
                () -> Assertions.assertEquals(1, interceptor2.getCount()),
                () -> Assertions.assertEquals(1, customInterceptor.getCount())
        );
    }

    private static void mockServerGet(List<Header> headers, String message, Integer value) {
        server.mock(
                server.requestBuilder()
                        .withHeaders(headers),
                server.responseBuilder(200, createPayload(message, value))
        );
    }

    private static JSONObject createPayload(String message, Integer value) {
        JSONObject payload = new JSONObject();

        payload.put("message", message);
        payload.put("value", value);

        return payload;
    }
}
