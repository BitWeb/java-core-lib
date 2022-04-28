package ee.bitweb.core.retrofit;

import ee.bitweb.core.retrofit.builder.RetrofitApiBuilder;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.core.retrofit.helpers.MockServerHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.integration.ClientAndServer;
import org.springframework.http.HttpStatus;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class RetrofitExecutorTests {

    private static final String BASE_URL = "http://localhost:12349";
    private static ClientAndServer externalService;

    @BeforeAll
    public static void setup() {
        externalService = ClientAndServer.startClientAndServer(12349);
    }

    @BeforeEach
    void reset() {
        externalService.reset();
    }

    @Test
    void onSuccessfulRequestShouldReturnResult() {
        ExternalServiceApi api = RetrofitApiBuilder.create(BASE_URL, ExternalServiceApi.class).build();

        MockServerHelper.setupMockGetRequest(
                externalService,
                "/data-request",
                200,
                1,
                wrapInResponse(createPayload("message", 1)).toString()
        );

        ExternalServiceApi.Payload response = RetrofitRequestExecutor.execute(api.getWrappedInResponse());

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue())
        );
    }

    @Test
    void onServiceErrorShouldThrowRetrofitException() {
        ExternalServiceApi api = RetrofitApiBuilder.create(BASE_URL, ExternalServiceApi.class).build();

        MockServerHelper.setupMockGetRequest(
                externalService,
                "/data-request",
                500,
                1,
                "SOME CUSTOM ERROR MESSAGE"
        );

        RetrofitException exception = Assertions.assertThrows(
                RetrofitException.class,
                () -> RetrofitRequestExecutor.execute(api.getWrappedInResponse())
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "UNSUCCESSFUL_REQUEST_ERROR : Request url: http://localhost:12349/data-request, " +
                                "status: 500 INTERNAL_SERVER_ERROR, body: SOME CUSTOM ERROR MESSAGE",
                        exception.getMessage()
                ),
                () -> Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus()),
                () -> Assertions.assertEquals("http://localhost:12349/data-request", exception.getUrl()),
                () -> Assertions.assertEquals("SOME CUSTOM ERROR MESSAGE", exception.getErrorBody())
        );
    }

    @Test
    void onEmptyDataResponseShouldThrowRetrofitException() {
        ExternalServiceApi api = RetrofitApiBuilder.create(BASE_URL, ExternalServiceApi.class).build();

        MockServerHelper.setupMockGetRequest(
                externalService,
                "/data-request",
                200,
                1,
                wrapInResponse(null).toString()
        );

        RetrofitException exception = Assertions.assertThrows(
                RetrofitException.class,
                () -> RetrofitRequestExecutor.execute(api.getWrappedInResponse())
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "EMPTY_RESPONSE_BODY_ERROR : Request url: http://localhost:12349/data-request, " +
                                "status: 200 OK, body: null",
                        exception.getMessage()
                ),
                () -> Assertions.assertEquals(HttpStatus.OK, exception.getHttpStatus()),
                () -> Assertions.assertEquals("http://localhost:12349/data-request", exception.getUrl()),
                () -> Assertions.assertNull(exception.getErrorBody())
        );
    }

    @Test
    void onSuccessfulRawRequestShouldReturnResult() {
        ExternalServiceApi api = RetrofitApiBuilder.create(BASE_URL, ExternalServiceApi.class).build();

        MockServerHelper.setupMockGetRequest(
                externalService,
                "/request",
                200,
                1,
                createPayload("message", 1).toString()
        );

        ExternalServiceApi.Payload response = RetrofitRequestExecutor.executeRaw(api.get());

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue())
        );
    }

    @Test
    void onServiceErrorWithRawRequestShouldThrowRetrofitException() {
        ExternalServiceApi api = RetrofitApiBuilder.create(BASE_URL, ExternalServiceApi.class).build();

        MockServerHelper.setupMockGetRequest(
                externalService,
                "/request",
                500,
                1,
                "SOME CUSTOM ERROR MESSAGE"
        );

        RetrofitException exception = Assertions.assertThrows(
                RetrofitException.class,
                () -> RetrofitRequestExecutor.executeRaw(api.get())
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "UNSUCCESSFUL_REQUEST_ERROR : Request url: http://localhost:12349/request, " +
                                "status: 500 INTERNAL_SERVER_ERROR, body: SOME CUSTOM ERROR MESSAGE",
                        exception.getMessage()
                ),
                () -> Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus()),
                () -> Assertions.assertEquals("http://localhost:12349/request", exception.getUrl()),
                () -> Assertions.assertEquals("SOME CUSTOM ERROR MESSAGE", exception.getErrorBody())
        );
    }

    @Test
    void onConnectionExceptionShouldThrowRetrofitException() {
        ExternalServiceApi api = RetrofitApiBuilder.create(
                "http://some-random-url",
                ExternalServiceApi.class
        ).build();

        RetrofitException exception = Assertions.assertThrows(
                RetrofitException.class,
                () -> RetrofitRequestExecutor.executeRaw(api.get())
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "REQUEST_ERROR : Request url: http://some-random-url/request, " +
                                "status: null, body: null",
                        exception.getMessage()
                ),
                () -> Assertions.assertNull(exception.getHttpStatus()),
                () -> Assertions.assertEquals("http://some-random-url/request", exception.getUrl()),
                () -> Assertions.assertNull(exception.getErrorBody())
        );
    }

    private static JSONObject wrapInResponse(JSONObject data) {
        JSONObject response = new JSONObject();
        response.put("data", data);

        return response;
    }

    private static JSONObject createPayload(String message, Integer value) {
        JSONObject payload = new JSONObject();

        payload.put("message", message);
        payload.put("value", value);

        return payload;
    }
}
