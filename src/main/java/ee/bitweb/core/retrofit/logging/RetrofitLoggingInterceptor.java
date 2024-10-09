package ee.bitweb.core.retrofit.logging;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import okio.GzipSource;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static okhttp3.internal.http.HttpHeaders.promisesBody;

@Slf4j
public class RetrofitLoggingInterceptor implements LoggingInterceptor {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";

    private LoggingLevel loggingLevel = LoggingLevel.BASIC;
    private final Set<String> redactHeaders = new HashSet<>();

    @Override
    public LoggingInterceptor setLevel(LoggingLevel level) {
        this.loggingLevel = level;

        return this;
    }

    @Override
    public void redactHeader(String name) {
        this.redactHeaders.add(name.toLowerCase());
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        if (loggingLevel.getLevel() == 0) {
            return chain.proceed(chain.request());
        }

        var request = chain.request();
        var connection = chain.connection();

        try {
            StringBuilder sb = new StringBuilder();

            sb.append(getRequestDescription(request, connection));

            if (loggingLevel.getLevel() >= 2) {
                sb.append("\n").append(getRequestHeaders(request));
            }

            if (loggingLevel.getLevel() >= 3) {
                sb.append("\n").append(getRequestBody(request));
            }

            log.info("{}", sb);
        } catch (Exception e) {
            log.error("Unable to log request.", e);
        }

        var startNs = System.nanoTime();

        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log.warn("<-- HTTP FAILED", e);
            throw e;
        }

        var durationMs = NANOSECONDS.toMillis(System.nanoTime() - startNs);

        try {
            StringBuilder sb = new StringBuilder();

            sb.append(getResponseDescription(response, durationMs));

            if (loggingLevel.getLevel() >= 2) {
                sb.append("\n").append(getResponseHeaders(response));
            }

            if (loggingLevel.getLevel() >= 3) {
                sb.append("\n").append(getResponseBody(response));
            }

            log.info("{}", sb);
        } catch (Exception e) {
            log.error("Unable to log response", e);
        }

        return response;
    }

    protected String getRequestDescription(Request request, Connection connection) {
        return "--> %s %s%s".formatted(request.method(), request.url(), connection != null ? connection.protocol() : "");
    }

    protected String getResponseDescription(Response response, long durationMs) {
        String bodySize;

        if (response.body() != null && response.body().contentLength() != -1L) {
            bodySize = "%s-byte".formatted(response.body().contentLength());
        } else {
            bodySize = "unknown length";
        }

        return "<-- %s%s %s %sms, body size %s".formatted(
                response.code(),
                response.message().isEmpty() ? "" : " " + response.message(),
                response.request().url(),
                durationMs,
                bodySize
        );
    }

    protected String getRequestHeaders(Request request) throws IOException {
        List<String> headerRows = new ArrayList<>();

        var requestHeaders = request.headers();
        var requestBody = request.body();

        String contentType = getRequestContentTypeValue(requestHeaders, requestBody);
        String contentLength = getRequestContentLengthValue(requestHeaders, requestBody);

        if (contentType != null) {
            addHeaderToRows(headerRows, CONTENT_TYPE, contentType);
        }

        if (contentLength != null) {
            addHeaderToRows(headerRows, CONTENT_LENGTH, contentLength);
        }

        for (int i = 0; i < requestHeaders.size(); i++) {
            var name = requestHeaders.name(i);
            if (List.of("content-type", "content-length").contains(name.toLowerCase())) {
                continue;
            }
            addHeaderToRows(headerRows, name, requestHeaders.value(i));
        }

        return headerRowsToString(headerRows);
    }

    protected String getResponseHeaders(Response response) {
        List<String> headerRows = new ArrayList<>();

        var responseHeaders = response.headers();
        var responseBody = response.body();

        String contentType = getResponseContentTypeValue(responseHeaders, responseBody);
        String contentLength = getResponseContentLengthValue(responseHeaders, responseBody);

        if (contentType != null) {
            addHeaderToRows(headerRows, CONTENT_TYPE, contentType);
        }

        if (contentLength != null) {
            addHeaderToRows(headerRows, CONTENT_LENGTH, contentLength);
        }

        for (int i = 0; i < responseHeaders.size(); i++) {
            var name = responseHeaders.name(i);
            if (List.of("content-type", "content-length").contains(name.toLowerCase())) {
                continue;
            }
            addHeaderToRows(headerRows, name, responseHeaders.value(i));
        }

        return headerRowsToString(headerRows);
    }

    protected String getRequestContentTypeValue(Headers headers, RequestBody body) {
        var headerValue = headers.get(CONTENT_TYPE);

        if (headerValue != null) {
            return headerValue;
        }

        if (body != null && body.contentType() != null) {
            return Objects.requireNonNullElse(body.contentType(), "").toString();
        }

        return null;
    }

    protected String getRequestContentLengthValue(Headers headers, RequestBody body) throws IOException {
        var headerValue = headers.get(CONTENT_LENGTH);

        if (headerValue != null) {
            return headerValue;
        }

        if (body != null && body.contentLength() != -1) {
            return String.valueOf(body.contentLength());
        }

        return null;
    }

    protected String getResponseContentTypeValue(Headers headers, ResponseBody body) {
        var headerValue = headers.get(CONTENT_TYPE);

        if (headerValue != null) {
            return headerValue;
        }

        if (body != null && body.contentType() != null) {
            return Objects.requireNonNullElse(body.contentType(), "").toString();
        }

        return null;
    }

    protected String getResponseContentLengthValue(Headers headers, ResponseBody body) {
        var headerValue = headers.get(CONTENT_LENGTH);

        if (headerValue != null) {
            return headerValue;
        }

        if (body != null && body.contentLength() != -1) {
            return String.valueOf(body.contentLength());
        }

        return null;
    }

    protected void addHeaderToRows(List<String> headerRows, String name, String value) {
        headerRows.add("\t%s: %s".formatted(
                name,
                redactHeaders.contains(name.toLowerCase()) ? " " : value
        ));
    }

    protected String headerRowsToString(List<String> headerRows) {
        StringBuilder sb = new StringBuilder()
                .append("Headers:");

        headerRows.forEach(row -> sb.append("\n").append(row));

        return sb.toString();
    }

    protected String getRequestBody(Request request) throws IOException {
        var body = request.body();

        if (body == null) {
            return "null";
        } else if (bodyHasUnknownEncoding(request.headers())) {
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

            if (isProbablyUtf8(buffer)) {
                assert charSet != null;
                return buffer.readString(charSet);
            } else {
                return ("binary body omitted");
            }
        }
    }

    protected String getResponseBody(Response response) throws IOException {
        if (!promisesBody(response)) {
            return "";
        } else if (bodyHasUnknownEncoding(response.headers())) {
            return "(encoded body omitted)";
        } else if (response.body() == null) {
            return "(body missing)";
        } else {
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

            if (!isProbablyUtf8(buffer)) {
                return "(binary %s-byte body omitted)".formatted(buffer.size());
            }

            if (contentLength != 0L) {
                return buffer.clone().readString(charset);
            } else {
                return "";
            }
        }
    }

    protected boolean bodyHasUnknownEncoding(Headers headers) {
        var contentEncoding = headers.get("Content-Encoding");

        if (contentEncoding == null) {
            return false;
        }

        return !contentEncoding.equalsIgnoreCase("identity") &&
                !contentEncoding.equalsIgnoreCase("gzip");
    }

    protected boolean isProbablyUtf8(Buffer buffer) {
        try {
            var prefix = new Buffer();
            var byteCount = buffer.size() > 64 ? 64 : buffer.size();

            buffer.copyTo(prefix, 0, byteCount);

            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }

                var codePoint = prefix.readUtf8CodePoint();

                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }

            return true;
        } catch (EOFException e) {
            log.trace("", e);
            return false;
        }
    }
}
