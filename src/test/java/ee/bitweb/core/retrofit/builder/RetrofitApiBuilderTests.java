package ee.bitweb.core.retrofit.builder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.retrofit.helpers.MockServerHelper;
import ee.bitweb.core.retrofit.helpers.RequestCountInterceptor;
import okhttp3.OkHttpClient;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.integration.ClientAndServer;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RetrofitApiBuilderTests {

    private static final String BASE_URL = "http://localhost:12345";
    private static ClientAndServer externalService;

    @BeforeAll
    public static void setup() {
        externalService = ClientAndServer.startClientAndServer(12345);
    }

    @BeforeEach
    void reset() {
        externalService.reset();
    }

    @Test
    void defaultBuilderWorksAsExpected() throws Exception {
        mockServerGet(externalService, "message", "1");
        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL,
                ExternalServiceApi.class
        ).build();

        ExternalServiceApi.Payload response = api.get().execute().body();
        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue())
        );
    }

    @Test
    void addedInterceptorShouldBeUsedOnRequest() throws Exception {
        mockServerGet(externalService, "message", "1");
        RequestCountInterceptor customInterceptor = new RequestCountInterceptor();

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL,
                ExternalServiceApi.class
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
        mockServerGet(externalService, "message", "1");
        RequestCountInterceptor customInterceptor1 = new RequestCountInterceptor();
        RequestCountInterceptor customInterceptor2 = new RequestCountInterceptor();

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL,
                ExternalServiceApi.class
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
        mockServerGet(externalService, "message", "1");
        RequestCountInterceptor interceptor = new RequestCountInterceptor();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().addInterceptor(interceptor);

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL,
                ExternalServiceApi.class
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
        mockServerGet(externalService, "message", "1");
        RequestCountInterceptor interceptor = new RequestCountInterceptor();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().addInterceptor(interceptor);

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL,
                ExternalServiceApi.class
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
        mockServerGet(externalService, "message", "1");

        RequestCountInterceptor customInterceptor1 = new RequestCountInterceptor();
        RequestCountInterceptor customInterceptor2 = new RequestCountInterceptor();

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL,
                ExternalServiceApi.class
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
    void addedCustomConverterIsApplied() throws Exception {
        mockServerGet(externalService, "message", 1.1);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        Converter.Factory converter = JacksonConverterFactory.create(mapper);

        ExternalServiceApi api = RetrofitApiBuilder.create(
                BASE_URL,
                ExternalServiceApi.class
        ).converter(
                converter
        ).build();

        ExternalServiceApi.Payload response = api.get().execute().body();

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue())
        );
    }

    private static void mockServerGet(ClientAndServer server, String message, Object value) {
        MockServerHelper.setupMockGetRequest(
                server,
                "/request",
                200,
                1,
                createPayload(message, value).toString()
        );
    }

    private static JSONObject createPayload(String message, Object value) {
        JSONObject payload = new JSONObject();

        payload.put("message", message);
        payload.put("value", value);

        return payload;
    }
}
