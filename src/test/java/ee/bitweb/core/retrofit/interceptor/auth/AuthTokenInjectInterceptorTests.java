package ee.bitweb.core.retrofit.interceptor.auth;

import ee.bitweb.core.retrofit.builder.RetrofitApiBuilder;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.retrofit.helpers.MockServerHelper;
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
public class AuthTokenInjectInterceptorTests {

    private static final String BASE_URL = "http://localhost:12350";
    private static final String HEADER_NAME = "x-auth-token";
    private static ClientAndServer externalService;

    @Mock
    private TokenProvider provider;

    @BeforeAll
    public static void setup() {
        externalService = ClientAndServer.startClientAndServer(12350);
    }

    @BeforeEach
    void reset() {
        externalService.reset();
    }

    @Test
    void onExistingTokenShouldBeAddedToRequest() throws Exception {
        Mockito.doReturn("token-value").when(provider).get();

        MockServerHelper.setupMockGetRequest(
                externalService,
                "/request",
                List.of(
                        new Header(HEADER_NAME, "token-value")
                ),
                200,
                1,
                createPayload("message", 1).toString()
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", createApi().get().execute().body().getMessage())
        );
    }

    @Test
    void onMissingTraceIdInContextShouldThrowException() throws Exception {
        MockServerHelper.setupMockGetRequest(
                externalService,
                "/request",
                200,
                1,
                createPayload("message", 1).toString()
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", createApi().get().execute().body().getMessage())
        );
    }

    private ExternalServiceApi createApi() {

        return RetrofitApiBuilder.create(
                BASE_URL,
                ExternalServiceApi.class
        ).add(
                new AuthTokenInjectInterceptor(HEADER_NAME, provider)
        ).build();
    }

    private static JSONObject createPayload(String message, Integer value) {
        JSONObject payload = new JSONObject();

        payload.put("message", message);
        payload.put("value", value);

        return payload;
    }
}
