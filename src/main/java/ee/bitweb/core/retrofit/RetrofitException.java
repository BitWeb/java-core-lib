package ee.bitweb.core.retrofit;

import ee.bitweb.core.exception.CoreException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import retrofit2.Call;

import java.io.IOException;

@Getter
public class RetrofitException extends CoreException {

    private final String url;
    private final HttpStatus httpStatus;
    private final String errorBody;

    public RetrofitException(String message, String url, HttpStatus status, String responseBody) {
        super(String.format("%s : %s", message, generateRequestMessage(url, status, responseBody)));
        this.url = url;
        this.httpStatus = status;
        this.errorBody = responseBody;
    }

    public static <T> RetrofitException of(String message, Call<T> request, retrofit2.Response<T> response) {
        return new RetrofitException(
                message,
                extractUrl(request),
                extractStatus(response),
                extractErrorBody(response)
        );
    }

    private static String generateRequestMessage(String url, HttpStatus status, String errorBody) {
        return String.format(
                "Request url: %s, status: %s, body: %s",
                url,
                status,
                errorBody
        );
    }

    private static <T> String extractUrl(Call<T> request) {
        if (request != null && request.request() != null && request.request().url() != null) {
            return request.request().url().toString();
        }

        return null;
    }

    private static <T> HttpStatus extractStatus(retrofit2.Response<T> response) {
        return response != null ? HttpStatus.valueOf(response.code()) : null;
    }

    private static <T> String extractErrorBody(retrofit2.Response<T> response) {
        try {
            return response != null && response.errorBody() != null ? response.errorBody().string() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
