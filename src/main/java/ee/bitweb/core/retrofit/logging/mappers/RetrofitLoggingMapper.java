package ee.bitweb.core.retrofit.logging.mappers;

import ee.bitweb.core.exception.CoreException;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface RetrofitLoggingMapper {

    String getValue(Request request, @Nullable Response response);

    String getKey();

    default void map(Request request, @Nullable Response response, Map<String, String> container) {
        if (container.containsKey(getKey())) {
            throw new CoreException(String.format("Retrofit log container already contains value for key %s", getKey()));
        }

        container.put(getKey(), getValue(request, response));
    }
}
