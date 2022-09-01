package ee.bitweb.core.retrofit;

import ee.bitweb.core.retrofit.builder.RetrofitApiBuilder;
import ee.bitweb.core.retrofit.helpers.ExternalServiceApi;
import ee.bitweb.http.server.mock.MockServer;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class RetrofitExecutorTests {

    private static final String BASE_URL = "http://localhost:";

    @RegisterExtension
    private static final MockServer server = new MockServer(HttpMethod.GET, "/data-request");

    private static ExternalServiceApi api;

    @BeforeAll
    public static void beforeAll() {
        api = RetrofitApiBuilder.create(BASE_URL + server.getPort(), ExternalServiceApi.class).build();
    }

    @Test
    void onSuccessfulRequestShouldReturnResult() {
        server.mock(
                server.requestBuilder(),
                server.responseBuilder(200)
                        .withBody(
                                wrapInResponse(createPayload("message", 1)).toString()
                        )
        );

        ExternalServiceApi.Payload response = RetrofitRequestExecutor.execute(api.getWrappedInResponse());

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue())
        );
    }

    @Test
    void onServiceErrorShouldThrowRetrofitException() {
        server.mock(
                server.requestBuilder(),
                server.responseBuilder(500)
                        .withBody("SOME CUSTOM ERROR MESSAGE")
        );

        RetrofitException exception = Assertions.assertThrows(
                RetrofitException.class,
                () -> RetrofitRequestExecutor.execute(api.getWrappedInResponse())
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "UNSUCCESSFUL_REQUEST_ERROR : Request url: " + BASE_URL + server.getPort() + "/data-request, " +
                                "status: 500 INTERNAL_SERVER_ERROR, body: SOME CUSTOM ERROR MESSAGE",
                        exception.getMessage()
                ),
                () -> Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus()),
                () -> Assertions.assertEquals(BASE_URL + server.getPort() + "/data-request", exception.getUrl()),
                () -> Assertions.assertEquals("SOME CUSTOM ERROR MESSAGE", exception.getErrorBody())
        );
    }

    @Test
    void onEmptyDataResponseShouldThrowRetrofitException() {
        server.mock(
                server.requestBuilder(),
                server.responseBuilder(200)
                        .withBody(
                                wrapInResponse(null).toString()
                        )
        );

        RetrofitException exception = Assertions.assertThrows(
                RetrofitException.class,
                () -> RetrofitRequestExecutor.execute(api.getWrappedInResponse())
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "EMPTY_RESPONSE_BODY_ERROR : Request url: " + BASE_URL + server.getPort() + "/data-request, " +
                                "status: 200 OK, body: null",
                        exception.getMessage()
                ),
                () -> Assertions.assertEquals(HttpStatus.OK, exception.getHttpStatus()),
                () -> Assertions.assertEquals(BASE_URL + server.getPort() + "/data-request", exception.getUrl()),
                () -> Assertions.assertNull(exception.getErrorBody())
        );
    }

    @Test
    void onSuccessfulRawRequestShouldReturnResult() {
        server.mock(
                server.requestBuilder("/request"),
                server.responseBuilder(200)
                        .withBody(
                                createPayload("message", 1).toString()
                        )
        );

        ExternalServiceApi.Payload response = RetrofitRequestExecutor.executeRaw(api.get());

        Assertions.assertAll(
                () -> Assertions.assertEquals("message", response.getMessage()),
                () -> Assertions.assertEquals(1, response.getValue())
        );
    }

    @Test
    void onServiceErrorWithRawRequestShouldThrowRetrofitException() {
        server.mock(
                server.requestBuilder("/request"),
                server.responseBuilder(500)
                        .withBody("SOME CUSTOM ERROR MESSAGE")
        );

        RetrofitException exception = Assertions.assertThrows(
                RetrofitException.class,
                () -> RetrofitRequestExecutor.executeRaw(api.get())
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "UNSUCCESSFUL_REQUEST_ERROR : Request url: " + BASE_URL + server.getPort() + "/request, " +
                                "status: 500 INTERNAL_SERVER_ERROR, body: SOME CUSTOM ERROR MESSAGE",
                        exception.getMessage()
                ),
                () -> Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus()),
                () -> Assertions.assertEquals(BASE_URL + server.getPort() + "/request", exception.getUrl()),
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
