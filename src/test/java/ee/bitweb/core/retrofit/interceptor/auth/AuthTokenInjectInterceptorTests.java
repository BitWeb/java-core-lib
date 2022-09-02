package ee.bitweb.core.retrofit.interceptor.auth;

import ee.bitweb.core.retrofit.builder.RetrofitApiBuilder;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.BlacklistCriteria;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.WhitelistCriteria;
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
import org.mockserver.model.NottableString;

import java.util.List;
import java.util.regex.Pattern;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuthTokenInjectInterceptorTests {

    private static final String BASE_URL = "http://localhost:";
    private static final String HEADER_NAME = "x-auth-token";

    @RegisterExtension
    private static final MockServer server = new MockServer(HttpMethod.GET, "/request");

    @Mock
    private TokenProvider provider;


    @Test
    void onExistingTokenWithWhitelistPassingCriteriaShouldBeAddedToRequest() {
        Mockito.doReturn("token-value").when(provider).get();

        server.mock(
                server.requestBuilder().withHeaders(new Header(HEADER_NAME, "token-value")),
                server.responseBuilder(200)
                        .withBody(createPayload("message", 1).toString())

        );

        ExternalServiceApi api = createBuilder()
                .add(
                        new AuthTokenInjectInterceptor(
                                HEADER_NAME,
                                provider,
                                new WhitelistCriteria(
                                        List.of(
                                                Pattern.compile("^http?:\\/\\/localhost:\\d{3,5}\\/.*")
                                        )
                                )
                        )
                ).build();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", api.get().execute().body().getMessage())
        );
    }

    @Test
    void onMissingTokenShouldNotAddHeader() {
        server.mock(
                server.requestBuilder().withHeader(NottableString.not(HEADER_NAME)),
                server.responseBuilder(200)
                        .withBody(createPayload("message", 1).toString())

        );

        ExternalServiceApi api = createBuilder()
                .add(
                        new AuthTokenInjectInterceptor(
                                HEADER_NAME,
                                provider,
                                new WhitelistCriteria(
                                        List.of(
                                                Pattern.compile("^http?:\\/\\/localhost:\\d{3,5}\\/.*")
                                        )
                                )
                        )
                ).build();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", api.get().execute().body().getMessage())
        );
    }

    @Test
    void onNoMatchingWhitelistCriteriaShouldNotAddHeader() {
        Mockito.doReturn("token-value").when(provider).get();
        server.mock(
                server.requestBuilder().withHeader(NottableString.not(HEADER_NAME)),
                server.responseBuilder(200)
                        .withBody(createPayload("message", 1).toString())
        );

        ExternalServiceApi api = createBuilder()
                .add(
                        new AuthTokenInjectInterceptor(
                                HEADER_NAME,
                                provider,
                                new WhitelistCriteria(
                                        List.of(
                                                Pattern.compile("^http?:\\/\\/localhost")
                                        )
                                )
                        )
                ).build();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", api.get().execute().body().getMessage())
        );
    }

    @Test
    void onNoMatchingBlacklistCriteriaShouldAddHeader() {
        Mockito.doReturn("token-value").when(provider).get();
        server.mock(
                server.requestBuilder().withHeaders(new Header(HEADER_NAME, "token-value")),
                server.responseBuilder(200)
                        .withBody(createPayload("message", 1).toString())

        );

        ExternalServiceApi api = createBuilder()
                .add(
                        new AuthTokenInjectInterceptor(
                                HEADER_NAME,
                                provider,
                                new BlacklistCriteria(
                                        List.of(
                                                Pattern.compile("^http?:\\/\\/localhost")
                                        )
                                )
                        )
                ).build();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", api.get().execute().body().getMessage())
        );
    }

    @Test
    void onMatchingBlacklistCriteriaShouldNotAddHeader() {
        Mockito.doReturn("token-value").when(provider).get();

        server.mock(
                server.requestBuilder().withHeader(NottableString.not(HEADER_NAME)),
                server.responseBuilder(200)
                        .withBody(createPayload("message", 1).toString())

        );

        ExternalServiceApi api = createBuilder()
                .add(
                        new AuthTokenInjectInterceptor(
                                HEADER_NAME,
                                provider,
                                new BlacklistCriteria(
                                        List.of(
                                                Pattern.compile("^http?:\\/\\/localhost:\\d{3,5}\\/.*")
                                        )
                                )
                        )
                ).build();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", api.get().execute().body().getMessage())
        );
    }

    private RetrofitApiBuilder<ExternalServiceApi> createBuilder() {
        return RetrofitApiBuilder.create(
                BASE_URL + server.getPort(),
                ExternalServiceApi.class
        );
    }

    private static JSONObject createPayload(String message, Integer value) {
        JSONObject payload = new JSONObject();

        payload.put("message", message);
        payload.put("value", value);

        return payload;
    }
}
