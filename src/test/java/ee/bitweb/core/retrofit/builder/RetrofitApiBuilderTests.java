package ee.bitweb.core.retrofit.builder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.retrofit.helpers.RequestCountInterceptor;
import ee.bitweb.http.server.mock.MockServer;
import io.netty.handler.codec.http.HttpMethod;
import okhttp3.OkHttpClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RetrofitApiBuilderTests {

    private static final String BASE_URL = "http://localhost:";

    @RegisterExtension
    private static final MockServer server = new MockServer(HttpMethod.GET, "/request");

    @Test
    void defaultBuilderWorksAsExpected() throws Exception {
        mockServerGet("message", "1");
        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL + server.getPort(),
                ExternalServiceApi.class,
                null
        ).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue())
        );
    }

    @Test
    void addedInterceptorShouldBeUsedOnRequest() throws Exception {
        mockServerGet("message", "1");
        RequestCountInterceptor customInterceptor = new RequestCountInterceptor();

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL + server.getPort(),
                ExternalServiceApi.class,
                null
        ).add(
                customInterceptor
        ).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue()),
                () -> Assertions.assertEquals(1, customInterceptor.getCount())
        );
    }

    @Test
    void addedMultipleInterceptorsShouldAllBeUsedOnRequest() throws Exception {
        mockServerGet("message", "1");
        RequestCountInterceptor customInterceptor1 = new RequestCountInterceptor();
        RequestCountInterceptor customInterceptor2 = new RequestCountInterceptor();

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL + server.getPort(),
                ExternalServiceApi.class,
                null
        ).addAll(
                List.of(
                        customInterceptor1,
                        customInterceptor2
                )
        ).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue()),
                () -> Assertions.assertEquals(1, customInterceptor1.getCount()),
                () -> Assertions.assertEquals(1, customInterceptor2.getCount())
        );
    }

    @Test
    void onAddedCustomClientIsApplied() throws Exception {
        mockServerGet("message", "1");
        RequestCountInterceptor interceptor = new RequestCountInterceptor();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().addInterceptor(interceptor);

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL + server.getPort(),
                ExternalServiceApi.class,
                null
        ).clientBuilder(
                clientBuilder
        ).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue()),
                () -> Assertions.assertEquals(1, interceptor.getCount())
        );
    }

    @Test
    void clearInterceptorsRemovesInterceptors() throws Exception {
        mockServerGet("message", "1");
        RequestCountInterceptor interceptor = new RequestCountInterceptor();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().addInterceptor(interceptor);

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL + server.getPort(),
                ExternalServiceApi.class,
                null
        ).clientBuilder(
                clientBuilder
        ).emptyInterceptors().build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue()),
                () -> Assertions.assertEquals(0, interceptor.getCount())
        );
    }

    @Test
    void removeInterceptorRemovesProvidedInterceptorOnly() throws Exception {
        mockServerGet("message", "1");

        RequestCountInterceptor customInterceptor1 = new RequestCountInterceptor();
        RequestCountInterceptor customInterceptor2 = new RequestCountInterceptor();

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL + server.getPort(),
                ExternalServiceApi.class,
                null
        ).addAll(
                List.of(
                        customInterceptor1,
                        customInterceptor2
                )
        ).remove(customInterceptor2).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue()),
                () -> Assertions.assertEquals(1, customInterceptor1.getCount()),
                () -> Assertions.assertEquals(0, customInterceptor2.getCount())
        );
    }

    @Test
    void callTimeoutIsApplied() {
        OkHttpClient.Builder clientBuilder = Mockito.spy(OkHttpClient.Builder.class);
        AtomicReference<OkHttpClient> clientRef = new AtomicReference<>();

        Mockito.doAnswer((answer) -> {
            OkHttpClient client = (OkHttpClient) answer.callRealMethod();
            clientRef.set(client);
            return client;
        }).when(clientBuilder).build();

        ExternalServiceApi api = RetrofitApiBuilder
                .create(BASE_URL + server.getPort(), ExternalServiceApi.class, null)
                .clientBuilder(clientBuilder)
                .callTimeout(999)
                .build();

        Assertions.assertEquals(999, clientRef.get().callTimeoutMillis());
    }

    @Test
    void connectTimeoutIsApplied() {
        OkHttpClient.Builder clientBuilder = Mockito.spy(OkHttpClient.Builder.class);
        AtomicReference<OkHttpClient> clientRef = new AtomicReference<>();

        Mockito.doAnswer((answer) -> {
            OkHttpClient client = (OkHttpClient) answer.callRealMethod();
            clientRef.set(client);
            return client;
        }).when(clientBuilder).build();

        ExternalServiceApi api = RetrofitApiBuilder
                .create(BASE_URL + server.getPort(), ExternalServiceApi.class, null)
                .clientBuilder(clientBuilder)
                .connectTimeout(999)
                .build();

        Assertions.assertEquals(999, clientRef.get().connectTimeoutMillis());
    }

    @Test
    void readTimeoutIsApplied() {
        OkHttpClient.Builder clientBuilder = Mockito.spy(OkHttpClient.Builder.class);
        AtomicReference<OkHttpClient> clientRef = new AtomicReference<>();

        Mockito.doAnswer((answer) -> {
            OkHttpClient client = (OkHttpClient) answer.callRealMethod();
            clientRef.set(client);
            return client;
        }).when(clientBuilder).build();

        ExternalServiceApi api = RetrofitApiBuilder
                .create(BASE_URL + server.getPort(), ExternalServiceApi.class, null)
                .clientBuilder(clientBuilder)
                .readTimeout(999)
                .build();

        Assertions.assertEquals(999, clientRef.get().readTimeoutMillis());
    }

    @Test
    void writeTimeoutIsApplied() {
        OkHttpClient.Builder clientBuilder = Mockito.spy(OkHttpClient.Builder.class);
        AtomicReference<OkHttpClient> clientRef = new AtomicReference<>();

        Mockito.doAnswer((answer) -> {
            OkHttpClient client = (OkHttpClient) answer.callRealMethod();
            clientRef.set(client);
            return client;
        }).when(clientBuilder).build();

        ExternalServiceApi api = RetrofitApiBuilder
                .create(BASE_URL + server.getPort(), ExternalServiceApi.class, null)
                .clientBuilder(clientBuilder)
                .writeTimeout(999)
                .build();

        Assertions.assertEquals(999, clientRef.get().writeTimeoutMillis());
    }

    @Test
    void addedCustomConverterIsApplied() throws Exception {
        mockServerGet("message", 1.1);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        Converter.Factory converter = JacksonConverterFactory.create(mapper);

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL + server.getPort(),
                ExternalServiceApi.class,
                null
        ).converter(
                converter
        ).build();

        ExternalServiceApi.Payload response = api.get().execute().body();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue())
        );
    }

    private static void mockServerGet(String message, Object value) {
        server.mock(
                server.requestBuilder(),
                server.responseBuilder(200, createPayload(message, value))
        );
    }

    private static JSONObject createPayload(String message, Object value) {
        JSONObject payload = new JSONObject();

        payload.put("message", message);
        payload.put("value", value);

        return payload;
    }
}
