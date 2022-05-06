package ee.bitweb.core.retrofit.interceptor.auth;

import ee.bitweb.core.retrofit.builder.RetrofitApiBuilder;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.retrofit.helpers.MockServerHelper;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.WhitelistCriteria;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import java.util.List;
import java.util.regex.Pattern;

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
    void onExistingTokenWithPassingCriteriaShouldBeAddedToRequest() throws Exception {
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

        ExternalServiceApi api = createBuilder()
                .add(
                        new AuthTokenInjectInterceptor(
                                HEADER_NAME,
                                provider,
                                new WhitelistCriteria(
                                        List.of(
                                                Pattern.compile("http://localhost")
                                        )
                                )
                        )
                ).build();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", api.get().execute().body().getMessage())
        );
    }

    @Test // TODO need to find a way to verify that header was missing
    void onMissingTokenShouldNotAddHeader() throws Exception {
        MockServerHelper.setupMockGetRequest(
                externalService,
                "/request",
                200,
                1,
                createPayload("message", 1).toString()
        );

        ExternalServiceApi api = createBuilder()
                .add(
                        new AuthTokenInjectInterceptor(
                                HEADER_NAME,
                                provider,
                                new WhitelistCriteria(List.of(Pattern.compile("http://localhost"))
                                )
                        )
                ).build();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", createApi().get().execute().body().getMessage())
        );

    }

    private RetrofitApiBuilder<ExternalServiceApi> createBuilder() {
        return RetrofitApiBuilder.create(
                BASE_URL,
                ExternalServiceApi.class
        );
    }

    private ExternalServiceApi createApi() {
        return createBuilder()
                .add(
                        new AuthTokenInjectInterceptor(
                                HEADER_NAME,
                                provider,
                                new WhitelistCriteria(List.of(Pattern.compile("http://localhost"))
                                )
                        )
                ).build();
    }

    private static JSONObject createPayload(String message, Integer value) {
        JSONObject payload = new JSONObject();

        payload.put("message", message);
        payload.put("value", value);

        return payload;
    }
}
