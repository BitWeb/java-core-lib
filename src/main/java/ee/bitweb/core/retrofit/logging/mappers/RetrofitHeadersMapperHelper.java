package ee.bitweb.core.retrofit.logging.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.Headers;
import okhttp3.MediaType;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RetrofitHeadersMapperHelper {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";

    public static String writeHeadersMapAsString(Map<String, String> headersMap) {
        return headersMap.entrySet().stream()
                .map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; "));
    }

    public static void addHeaderToResult(Set<String> redactHeaders, Map<String, String> result, String name, String value) {
        result.put(name, redactHeaders.contains(name.toLowerCase()) ? " " : value);
    }

    public static String getContentTypeValue(Headers headers, MediaType mediaType) {
        var headerValue = headers.get(CONTENT_TYPE);

        if (headerValue != null) {
            return headerValue;
        }

        if (mediaType == null) {
            return null;
        }

        return mediaType.type();
    }

    public static String getContentLengthValue(Headers headers, Long contentLength) {
        var headerValue = headers.get(CONTENT_LENGTH);

        if (headerValue != null) {
            return headerValue;
        }

        if (contentLength != null && contentLength != -1) {
            return String.valueOf(contentLength);
        }

        return null;
    }
}
