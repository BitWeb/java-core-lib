package ee.bitweb.core.retrofit.interceptor;

import ee.bitweb.core.retrofit.builder.RetrofitApiBuilder;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.invoker.http.TraceIdFilterConfig;
import ee.bitweb.http.server.mock.MockServer;
import io.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.model.Header;

import java.util.List;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TraceIdInterceptorTests {

    private static final String BASE_URL = "http://localhost:";

    @RegisterExtension
    private static final MockServer server = new MockServer(HttpMethod.GET, "/request");

    @Mock
    TraceIdContext context;

    @Mock
    TraceIdFilterConfig config;

    @Test
    void onExistingTraceIdItIsAddedToRequest() {
        Mockito.doReturn("some-trace-id-value").when(context).get();
        Mockito.doReturn("header-name").when(config).getHeaderName();

        createMockRequest();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", createApi().get().execute().body().getMessage())
        );
    }

    @Test
    void onMissingTraceIdInContextShouldThrowException() {
        Mockito.doReturn("header-name").when(config).getHeaderName();
        createMockRequest();
        ExternalServiceApi api = createApi();

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> api.get().execute()
        );

        Assertions.assertEquals("Cannot execute Retrofit request without trace id present", exception.getMessage());
    }

    private ExternalServiceApi createApi() {
        return RetrofitApiBuilder.create(
                BASE_URL  + server.getPort(),
                ExternalServiceApi.class
        ).add(
                new TraceIdInterceptor(config, context)
        ).build();
    }

    private void createMockRequest() {
        mockServerGet(
                List.of(
                        new Header(config.getHeaderName(), "some-trace-id-value")
                ),
                "message",
                1
        );
    }

    private static void mockServerGet(List<Header> headers, String message, Integer value) {
        server.mock(
                server.requestBuilder().withHeaders(headers),
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
