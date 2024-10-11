package ee.bitweb.core.retrofit.logging;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import okio.GzipSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static okhttp3.internal.http.HttpHeaders.promisesBody;

@Slf4j
public class RetrofitLoggingInterceptor implements LoggingInterceptor {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";

    private static final String REQUEST_METHOD_KEY = "RequestMethod";
    private static final String REQUEST_URL_KEY = "RequestUrl";
    private static final String REQUEST_PROTOCOL_KEY = "RequestProtocol";
    private static final String REQUEST_DURATION_KEY = "RequestDuration";
    private static final String REQUEST_BODY_SIZE_KEY = "RequestBodySize";
    private static final String REQUEST_HEADERS_KEY = "RequestHeaders";
    private static final String REQUEST_BODY_KEY = "RequestBody";
    private static final String RESPONSE_CODE_KEY = "ResponseCode";
    private static final String RESPONSE_MESSAGE_KEY = "ResponseMessage";
    private static final String RESPONSE_BODY_SIZE_KEY = "ResponseBodySize";
    private static final String RESPONSE_HEADERS_KEY = "ResponseHeaders";
    private static final String RESPONSE_BODY_KEY = "ResponseBody";

    private final List<LoggingLevel> loggingLevel = new ArrayList<>();
    private final Set<String> redactHeaders = new HashSet<>();
    private final Set<String> redactBodyUrls = new HashSet<>();

    private int maxLoggableRequestSize = Integer.MAX_VALUE;
    private int maxLoggableResponseSize = Integer.MAX_VALUE;

    @Override
    public LoggingInterceptor setLoggingLevel(LoggingLevel level) {
        this.loggingLevel.clear();

        List.of(LoggingLevel.BASIC, LoggingLevel.HEADERS, LoggingLevel.BODY).forEach(option -> {
            if (level.getLevel() >= option.getLevel()) {
                this.loggingLevel.add(option);
            }
        });

        return this;
    }

    @Override
    public LoggingInterceptor setMaxLoggableRequestSize(int size) {
        this.maxLoggableRequestSize = size;

        return this;
    }

    @Override
    public LoggingInterceptor setMaxLoggableResponseSize(int size) {
        this.maxLoggableResponseSize = size;

        return this;
    }

    @Override
    public LoggingInterceptor addRedactBodyURL(String url) {
        this.redactBodyUrls.add(url.toLowerCase());

        return this;
    }

    @Override
    public void redactHeader(String name) {
        this.redactHeaders.add(name.toLowerCase());
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        if (loggingLevel.isEmpty()) {
            return chain.proceed(chain.request());
        }

        Map<String, String> container = new HashMap<>();

        addRequestLogEntries(container, chain);

        var startNs = System.nanoTime();

        Response response;
        try {
            response = chain.proceed(chain.request());
        } catch (Exception e) {
            writeFailedLogMessage(container, e);
            throw e;
        }

        container.put(REQUEST_DURATION_KEY, String.valueOf(NANOSECONDS.toMillis(System.nanoTime() - startNs)));

        addResponseLogEntries(container, response);

        writeLogMessage(container);

        return response;
    }

    protected void addRequestLogEntries(Map<String, String> container, Chain chain) {
        var request = chain.request();
        var connection = chain.connection();

        try {
            container.put(REQUEST_METHOD_KEY, request.method());
            container.put(REQUEST_PROTOCOL_KEY, connection != null ? connection.protocol().toString() : "-");
            container.put(REQUEST_URL_KEY, request.url().toString());
            container.put(REQUEST_BODY_SIZE_KEY, request.body() != null ? String.valueOf(request.body().contentLength()) : "-");

            if (loggingLevel.contains(LoggingLevel.HEADERS)) {
                container.put(REQUEST_HEADERS_KEY, getRequestHeadersString(request));
            }

            if (loggingLevel.contains(LoggingLevel.BODY)) {
                container.put(REQUEST_BODY_KEY, getRequestBody(request));
            }
        } catch (Exception e) {
            log.error("Unable to log request.", e);
        }
    }

    protected void addResponseLogEntries(Map<String, String> container, Response response) {
        try {
            container.put(RESPONSE_CODE_KEY, String.valueOf(response.code()));
            container.put(RESPONSE_BODY_SIZE_KEY, response.body() != null ? String.valueOf(response.body().contentLength()) : "-");

            if (!response.message().isEmpty()) {
                container.put(RESPONSE_MESSAGE_KEY, response.message());
            }

            if (loggingLevel.contains(LoggingLevel.HEADERS)) {
                container.put(RESPONSE_HEADERS_KEY, addResponseHeaders(response));
            }

            if (loggingLevel.contains(LoggingLevel.BODY)) {
                container.put(RESPONSE_BODY_KEY, getResponseBody(response));
            }
        } catch (Exception e) {
            log.error("Unable to log response", e);
        }
    }

    protected String getRequestHeadersString(Request request) throws IOException {
        Map<String, String> result = new HashMap<>();

        var requestHeaders = request.headers();
        var requestBody = request.body();

        String contentType = getRequestContentTypeValue(requestHeaders, requestBody);
        String contentLength = getRequestContentLengthValue(requestHeaders, requestBody);

        if (contentType != null) {
            addHeaderToResult(result, CONTENT_TYPE, contentType);
        }

        if (contentLength != null) {
            addHeaderToResult(result, CONTENT_LENGTH, contentLength);
        }

        for (int i = 0; i < requestHeaders.size(); i++) {
            var name = requestHeaders.name(i);
            if (CONTENT_TYPE.equalsIgnoreCase(name) || CONTENT_LENGTH.equalsIgnoreCase(name)) {
                continue;
            }
            addHeaderToResult(result, name, requestHeaders.value(i));
        }

        return writeHeadersMapAsString(result);
    }

    protected String addResponseHeaders(Response response) {
        Map<String, String> result = new HashMap<>();

        var responseHeaders = response.headers();
        var responseBody = response.body();

        String contentType = getResponseContentTypeValue(responseHeaders, responseBody);
        String contentLength = getResponseContentLengthValue(responseHeaders, responseBody);

        if (contentType != null) {
            addHeaderToResult(result, CONTENT_TYPE, contentType);
        }

        if (contentLength != null) {
            addHeaderToResult(result, CONTENT_LENGTH, contentLength);
        }

        for (int i = 0; i < responseHeaders.size(); i++) {
            var name = responseHeaders.name(i);
            if (CONTENT_TYPE.equalsIgnoreCase(name) || CONTENT_LENGTH.equalsIgnoreCase(name)) {
                continue;
            }
            addHeaderToResult(result, name, responseHeaders.value(i));
        }

        return writeHeadersMapAsString(result);
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

    protected void addHeaderToResult(Map<String, String> result, String name, String value) {
        result.put(name, redactHeaders.contains(name.toLowerCase()) ? " " : value);
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
        } else if (isRedactBodyUrl(request.url().toString())) {
            return "(body redacted)";
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
                var bodyString = buffer.readString(charSet);

                if (request.body().contentLength() > maxLoggableRequestSize) {
                    return "%s ... Content size: %s characters".formatted(
                            bodyString.substring(0, maxLoggableRequestSize),
                            request.body().contentLength()
                    );
                }

                return bodyString;
            } else {
                return ("binary body omitted");
            }
        }
    }

    protected String getResponseBody(Response response) throws IOException {
        if (isRedactBodyUrl(response.request().url().toString())) {
            return "(body redacted)";
        } else if (!promisesBody(response)) {
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
                var bodyString = buffer.clone().readString(charset);
                if (contentLength > maxLoggableResponseSize) {
                    return "%s ... Content size: %s characters".formatted(
                            bodyString.substring(0, maxLoggableResponseSize),
                            contentLength
                    );
                }

                return bodyString;
            } else {
                return "";
            }
        }
    }

    protected boolean isRedactBodyUrl(String url) {
        return redactBodyUrls.contains(url);
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

    protected void writeFailedLogMessage(Map<String, String> container, Exception e) {
        Map<String, String> currentContext = MDC.getCopyOfContextMap();

        container.forEach(MDC::put);

        log.warn(
                "Failed to process request. Method({}), URL({})",
                get(container, REQUEST_METHOD_KEY),
                get(container, REQUEST_URL_KEY),
                e
        );

        if (currentContext != null) {
            MDC.setContextMap(currentContext);
        }
    }

    protected void writeLogMessage(Map<String, String> container) {
        Map<String, String> currentContext = MDC.getCopyOfContextMap();

        container.forEach(MDC::put);

        log.info(
                "Method({}), URL({}) Status({}) ResponseSize({}) Duration({} ms)",
                get(container, REQUEST_METHOD_KEY),
                get(container, REQUEST_URL_KEY),
                get(container, RESPONSE_CODE_KEY),
                get(container, RESPONSE_BODY_SIZE_KEY),
                get(container, REQUEST_DURATION_KEY)
        );

        if (currentContext != null) {
            MDC.setContextMap(currentContext);
        }
    }

    protected String get(Map<String, String> container, String key) {
        if (container.containsKey(key)) {
            return container.get(key);
        }

        return "-";
    }

    protected String writeHeadersMapAsString(Map<String, String> headersMap) {
        return headersMap.entrySet().stream()
                .map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; "));
    }
}
