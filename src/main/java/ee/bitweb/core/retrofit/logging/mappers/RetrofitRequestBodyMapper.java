package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RequiredArgsConstructor
public class RetrofitRequestBodyMapper implements RetrofitLoggingMapper {

    public static final String KEY = "RequestBody";

    private final int maxLoggableRequestSize;
    private final Set<String> redactBodyUrls;

    @Override
    public String getValue(Request request, Response response) {
        try {
            return getRequestBody(request);
        } catch (IOException e) {
            log.error("Failed to parse request body.", e);
            return "Parse error";
        }
    }

    @Override
    public String getKey() {
        return KEY;
    }

    /**
     * Stub method to be able to add custom sanitization of body. For example removing passwords and other sensitive data.
     *
     * @param body String representation of raw request body
     * @return sanitized body
     */
    protected String sanitizeBody(String body) {
        return body;
    }

    private String getRequestBody(Request request) throws IOException {
        var body = request.body();

        if (body == null) {
            return "null";
        } else if (RetrofitBodyMapperHelper.isRedactBodyUrl(redactBodyUrls, request.url().toString())) {
            return "(body redacted)";
        } else if (RetrofitBodyMapperHelper.bodyHasUnknownEncoding(request.headers())) {
            return "(encoded body omitted)";
        } else if (body.isDuplex()) {
            return "(duplex request body omitted)";
        } else if (body.isOneShot()) {
            return "(one-shot body omitted)";
        } else {
            var buffer = new Buffer();
            body.writeTo(buffer);

            var contentType = body.contentType();
            Charset charSet = contentType != null ? contentType.charset(UTF_8) : UTF_8;

            if (RetrofitBodyMapperHelper.isProbablyUtf8(buffer)) {
                assert charSet != null;
                var bodyString = buffer.readString(charSet);

                if (request.body() != null && request.body().contentLength() > maxLoggableRequestSize) {
                    return "%s ... Content size: %s characters".formatted(
                            bodyString.substring(0, maxLoggableRequestSize),
                            request.body().contentLength()
                    );
                }

                return sanitizeBody(bodyString);
            } else {
                return ("binary body omitted");
            }
        }
    }
}
