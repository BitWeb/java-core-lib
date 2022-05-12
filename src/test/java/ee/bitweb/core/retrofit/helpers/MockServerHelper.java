package ee.bitweb.core.retrofit.helpers;

import io.swagger.models.HttpMethod;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.util.concurrent.TimeUnit;

import static org.mockserver.model.HttpResponse.response;

public class MockServerHelper {

    public static void mock(ClientAndServer server, HttpRequest request, HttpResponse response) {
        server.when(request).respond(response);
    }

    public static HttpResponse responseBuilder(int statusCode) {
        return response()
                .withStatusCode(statusCode)
                .withDelay(new Delay(TimeUnit.MILLISECONDS, 150))
                .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8")
                );
    }

    public static HttpRequest requestBuilder(String path, HttpMethod method) {
        return HttpRequest.request()
                .withPath(path)
                .withMethod(method.name());
    }
}
