package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.GzipSource;

import java.io.IOException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static okhttp3.internal.http.HttpHeaders.promisesBody;

@Slf4j
@RequiredArgsConstructor
public class RetrofitResponseBodyMapper implements RetrofitLoggingMapper {

    public static final String KEY = "ResponseBody";

    private final Set<String> redactBodyUrls;
    private final int maxLoggableResponseSize;

    @Override
    public String getValue(Request request, Response response) {
        try {
            return getResponseBody(response);
        } catch (IOException e) {
            log.error("Failed to parse response body.", e);
            return "Parse error";
        }
    }

    @Override
    public String getKey() {
        return KEY;
    }

    protected String getResponseBody(Response response) throws IOException {
        if (RetrofitBodyMapperHelper.isRedactBodyUrl(redactBodyUrls, response.request().url().toString())) {
            return "(body redacted)";
        } else if (!promisesBody(response)) {
            return "";
        } else if (RetrofitBodyMapperHelper.bodyHasUnknownEncoding(response.headers())) {
            return "(encoded body omitted)";
        } else if (response.body() == null) {
            return "(body missing)";
        } else {
            return parseBody(response);
        }
    }

    /**
     * Stub method to be able to add custom sanitization of body. For example removing passwords and other sensitive data.
     *
     * @param body String representation of raw response body
     * @return sanitized body
     */
    protected String sanitizeBody(String body) {
        return body;
    }

    private String parseBody(Response response) throws IOException {
        var responseBody = response.body();
        var contentType = responseBody.contentType();
        var contentLength = responseBody.contentLength();
        var charset = contentType != null ? contentType.charset(UTF_8) : UTF_8;

        var source = responseBody.source();
        source.request(Long.MAX_VALUE);
        var buffer = source.getBuffer();

        Long gzippedLength = null;
        if ("gzip".equalsIgnoreCase(response.header("Content-Encoding"))) {
            gzippedLength = buffer.size();
            var gzipSource = new GzipSource(buffer.clone());
            buffer = new Buffer();
            gzipSource.read(buffer, gzippedLength);
        }

        if (!RetrofitBodyMapperHelper.isProbablyUtf8(buffer)) {
            return "(binary %s-byte body omitted)".formatted(buffer.size());
        }

        if (contentLength != 0L) {
            var bodyString = buffer.clone().readString(charset);
            if (contentLength > maxLoggableResponseSize) {
                return "%s ... Content size: %s characters".formatted(
                        bodyString.substring(0, maxLoggableResponseSize),
                        contentLength
                );
            }

            return sanitizeBody(bodyString);
        } else {
            return "";
        }
    }
}
