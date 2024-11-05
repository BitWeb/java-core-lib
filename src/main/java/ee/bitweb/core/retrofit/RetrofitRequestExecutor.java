package ee.bitweb.core.retrofit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RetrofitRequestExecutor {

    private static final String REQUEST_ERROR = "REQUEST_ERROR";
    private static final String UNSUCCESSFUL_REQUEST_ERROR = "UNSUCCESSFUL_REQUEST_ERROR";
    private static final String EMPTY_RESPONSE_BODY_ERROR = "EMPTY_RESPONSE_BODY_ERROR";

    public static <T> T execute(Call<Response<T>> request) {
        retrofit2.Response<Response<T>> response = doRequest(request);

        if (response.body() == null || response.body().getData() == null) {
            throw RetrofitException.of(EMPTY_RESPONSE_BODY_ERROR, request, response);
        }

        return response.body().getData();
    }

    public static <T> T executeRaw(Call<T> request) {
        return doRequest(request).body();
    }

    private static <T> retrofit2.Response<T> doRequest(Call<T> request) {
        retrofit2.Response<T> response;
        try {
            response = request.execute();
        } catch (Exception e) {
            log.error("Request failed: ", e);
            throw RetrofitException.of(REQUEST_ERROR, request, null);
        }

        validateResponse(response, request);

        return response;
    }

    private static <T> void validateResponse(retrofit2.Response<T> response, Call<T> request) {
        if (!response.isSuccessful()) {
            throw RetrofitException.of(UNSUCCESSFUL_REQUEST_ERROR, request, response);
        }
    }
}
