package ee.bitweb.core.retrofit.helpers;

import io.swagger.models.HttpMethod;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MockServerHelper {

    public static void setupMockGetRequest(
            ClientAndServer mockServer,
            String path,
            int code,
            int times,
            String response
    ) {
        setupMockRequest(
                mockServer,
                createMockRequestBuilder()
                        .withMethod(HttpMethod.GET.name())
                        .withPath(path),
                code,
                times,
                response
        );
    }

    public static void setupMockGetRequest(
            ClientAndServer mockServer,
            String path,
            List<Header> headers,
            int code,
            int times,
            String response
    ) {
        setupMockRequest(
                mockServer,
                createMockRequestBuilder()
                        .withMethod(HttpMethod.GET.name())
                        .withPath(path)
                        .withHeaders(headers)
                        .withHeaders(),
                code,
                times,
                response
        );
    }

    public static HttpRequest createMockRequestBuilder() {
        return request();
    }

    public static void setupMockRequest(
            ClientAndServer mockServer,
            HttpRequest request,
            int code,
            int times,
            String response
    ) {
        mockServer.when(
                        request, exactly(times)
                )
                .respond(
                        response()
                                .withStatusCode(code).withDelay(new Delay(TimeUnit.MILLISECONDS, 150))
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8")
                                )
                                .withBody(response)
                );
    }
}

