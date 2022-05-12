package ee.bitweb.core.retrofit.interceptor;

import ee.bitweb.core.retrofit.builder.RetrofitApiBuilder;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.retrofit.helpers.MockServerHelper;
import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.invoker.http.TraceIdFilterConfig;
import io.swagger.models.HttpMethod;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import java.util.List;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class TraceIdInterceptorTests {

    private static final String BASE_URL = "http://localhost:12347";
    private static ClientAndServer externalService;

    @Mock
    TraceIdContext context;

    @Mock
    TraceIdFilterConfig config;

    @BeforeAll
    public static void setup() {
        externalService = ClientAndServer.startClientAndServer(12347);
    }

    @BeforeEach
    void reset() {
        externalService.reset();
    }

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
                BASE_URL,
                ExternalServiceApi.class
        ).add(
                new TraceIdInterceptor(config, context)
        ).build();
    }

    private void createMockRequest() {
        mockServerGet(
                externalService,
                List.of(
                        new Header(config.getHeaderName(), "some-trace-id-value")
                ),
                "message",
                1
        );
    }

    private static void mockServerGet(ClientAndServer server, List<Header> headers, String message, Integer value) {
        MockServerHelper.mock(
                server,
                MockServerHelper.requestBuilder("/request", HttpMethod.GET)
                        .withHeaders(headers),
                MockServerHelper.responseBuilder(200)
                        .withBody(
                                createPayload(message, value).toString()
                        )
        );
    }

    private static JSONObject createPayload(String message, Integer value) {
        JSONObject payload = new JSONObject();

        payload.put("message", message);
        payload.put("value", value);

        return payload;
    }
}
