package ee.bitweb.core.retrofit.logging.mappers;

import ee.bitweb.core.exception.CoreException;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;

public interface RetrofitLoggingMapper {

    String getValue(Request request, Response response);

    String getKey();

    default void map(Request request, Response response, Map<String, String> container) {
        if (container.containsKey(getKey())) {
            throw new CoreException(String.format("Retrofit log container already contains value for key %s", getKey()));
        }

        container.put(getKey(), getValue(request, response));
    }
}
