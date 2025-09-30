package ee.bitweb.core.retrofit.logging.mappers;

import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RetrofitRequestBodyMapperTest {

    @Mock
    RequestBody requestBody;

    @Mock
    Request request;

    @Mock
    HttpUrl httpUrl;

    @Mock
    Headers headers;

    @Mock
    MediaType mediaType;

    private static Object setStringToBuffer(InvocationOnMock invocation) throws IOException {
        Buffer buffer = invocation.getArgument(0);

        buffer.readFrom(new ByteArrayInputStream("some data that may or may not be over limit".getBytes()));

        return null;
    }

    @Test
    @DisplayName("Body is null")
    void nullBody() {
        Mockito.when(request.body()).thenReturn(null);

        var mapper = new RetrofitRequestBodyMapper(2, new HashSet<>());

        assertEquals("null", mapper.getValue(request, null));

        Mockito.verify(request, Mockito.only()).body();
        Mockito.verifyNoMoreInteractions(request);
        Mockito.verifyNoInteractions(requestBody, httpUrl);
    }

    @Test
    @DisplayName("Body must be redacted")
    void redactBody() throws MalformedURLException {
        Mockito.when(httpUrl.url()).thenReturn(new URL("http://localhost:6542/"));
        Mockito.when(request.url()).thenReturn(httpUrl);
        Mockito.when(request.body()).thenReturn(requestBody);

        Set<String> redactBodyUrls = Set.of("http://localhost:6542/");
        var mapper = new RetrofitRequestBodyMapper(0, redactBodyUrls);

        try (MockedStatic<RetrofitBodyMapperHelper> mockedStatic = Mockito.mockStatic(RetrofitBodyMapperHelper.class)) {
            mockedStatic.when(() -> RetrofitBodyMapperHelper.isRedactBodyUrl(redactBodyUrls, "http://localhost:6542/")).thenReturn(true);

            assertEquals("(body redacted)", mapper.getValue(request, null));
        }

        Mockito.verify(request, Mockito.times(1)).body();
        Mockito.verify(request, Mockito.times(1)).url();
        Mockito.verify(httpUrl, Mockito.only()).url();
        Mockito.verifyNoMoreInteractions(request, httpUrl);
        Mockito.verifyNoInteractions(requestBody);
    }

    @Test
    @DisplayName("Body has unknown encoding")
    void unknownEncoding() throws MalformedURLException {
        Mockito.when(httpUrl.url()).thenReturn(new URL("http://localhost:6542/"));
        Mockito.when(request.url()).thenReturn(httpUrl);
        Mockito.when(request.body()).thenReturn(requestBody);
        Mockito.when(request.headers()).thenReturn(headers);

        Set<String> redactBodyUrls = Set.of();
        var mapper = new RetrofitRequestBodyMapper(0, redactBodyUrls);

        try (MockedStatic<RetrofitBodyMapperHelper> mockedStatic = Mockito.mockStatic(RetrofitBodyMapperHelper.class)) {
            mockedStatic.when(() -> RetrofitBodyMapperHelper.isRedactBodyUrl(redactBodyUrls, "http://localhost:6542/")).thenReturn(false);
            mockedStatic.when(() -> RetrofitBodyMapperHelper.bodyHasUnknownEncoding(headers)).thenReturn(true);

            assertEquals("(encoded body omitted)", mapper.getValue(request, null));
        }

        Mockito.verify(request, Mockito.times(1)).body();
        Mockito.verify(request, Mockito.times(1)).url();
        Mockito.verify(request, Mockito.times(1)).headers();
        Mockito.verify(httpUrl, Mockito.only()).url();
        Mockito.verifyNoMoreInteractions(request, httpUrl);
        Mockito.verifyNoInteractions(requestBody);
    }

    @Test
    @DisplayName("Duplex body")
    void duplexBody() throws MalformedURLException {
        Mockito.when(httpUrl.url()).thenReturn(new URL("http://localhost:6542/"));
        Mockito.when(request.url()).thenReturn(httpUrl);
        Mockito.when(request.body()).thenReturn(requestBody);
        Mockito.when(request.headers()).thenReturn(headers);
        Mockito.when(requestBody.isDuplex()).thenReturn(true);

        Set<String> redactBodyUrls = Set.of();
        var mapper = new RetrofitRequestBodyMapper(0, redactBodyUrls);

        try (MockedStatic<RetrofitBodyMapperHelper> mockedStatic = Mockito.mockStatic(RetrofitBodyMapperHelper.class)) {
            mockedStatic.when(() -> RetrofitBodyMapperHelper.isRedactBodyUrl(redactBodyUrls, "http://localhost:6542/")).thenReturn(false);
            mockedStatic.when(() -> RetrofitBodyMapperHelper.bodyHasUnknownEncoding(headers)).thenReturn(false);

            assertEquals("(duplex request body omitted)", mapper.getValue(request, null));
        }

        Mockito.verify(request, Mockito.times(1)).body();
        Mockito.verify(request, Mockito.times(1)).url();
        Mockito.verify(request, Mockito.times(1)).headers();
        Mockito.verify(requestBody, Mockito.times(1)).isDuplex();
        Mockito.verify(httpUrl, Mockito.only()).url();
        Mockito.verifyNoMoreInteractions(request, httpUrl, requestBody);
    }

    @Test
    @DisplayName("One-shot body")
    void oneShotBody() throws MalformedURLException {
        Mockito.when(httpUrl.url()).thenReturn(new URL("http://localhost:6542/"));
        Mockito.when(request.url()).thenReturn(httpUrl);
        Mockito.when(request.body()).thenReturn(requestBody);
        Mockito.when(request.headers()).thenReturn(headers);
        Mockito.when(requestBody.isDuplex()).thenReturn(false);
        Mockito.when(requestBody.isOneShot()).thenReturn(true);

        Set<String> redactBodyUrls = Set.of();
        var mapper = new RetrofitRequestBodyMapper(0, redactBodyUrls);

        try (MockedStatic<RetrofitBodyMapperHelper> mockedStatic = Mockito.mockStatic(RetrofitBodyMapperHelper.class)) {
            mockedStatic.when(() -> RetrofitBodyMapperHelper.isRedactBodyUrl(redactBodyUrls, "http://localhost:6542/")).thenReturn(false);
            mockedStatic.when(() -> RetrofitBodyMapperHelper.bodyHasUnknownEncoding(headers)).thenReturn(false);

            assertEquals("(one-shot body omitted)", mapper.getValue(request, null));
        }

        Mockito.verify(request, Mockito.times(1)).body();
        Mockito.verify(request, Mockito.times(1)).url();
        Mockito.verify(request, Mockito.times(1)).headers();
        Mockito.verify(requestBody, Mockito.times(1)).isDuplex();
        Mockito.verify(requestBody, Mockito.times(1)).isOneShot();
        Mockito.verify(httpUrl, Mockito.only()).url();
        Mockito.verifyNoMoreInteractions(request, httpUrl, requestBody);
    }

    @Test
    @DisplayName("Request body is not UTF-8")
    void requestBodyIsNotUtf8() throws IOException {
        Mockito.doNothing().when(requestBody).writeTo(Mockito.isA(Buffer.class));
        Mockito.when(requestBody.contentType()).thenReturn(MediaType.get("application/octet-stream"));
        var mapper = new RetrofitRequestBodyMapper(0, Set.of());

        try (MockedStatic<RetrofitBodyMapperHelper> mockedStatic = Mockito.mockStatic(RetrofitBodyMapperHelper.class)) {
            mockedStatic.when(() -> RetrofitBodyMapperHelper.isProbablyUtf8(Mockito.isA(Buffer.class))).thenReturn(false);

            assertEquals("(binary body omitted)", mapper.getBodyString(requestBody));
        }

        Mockito.verify(requestBody, Mockito.times(1)).writeTo(Mockito.isA(Buffer.class));
        Mockito.verifyNoMoreInteractions(requestBody);
        Mockito.verifyNoInteractions(request, httpUrl, headers);
    }

    @Test
    @DisplayName("Request body is returned even when content type is not available")
    void contentTypeIsNotAvailable() throws IOException {
        Mockito.doAnswer(RetrofitRequestBodyMapperTest::setStringToBuffer).when(requestBody).writeTo(Mockito.isA(Buffer.class));
        Mockito.when(requestBody.contentType()).thenReturn(null);
        Mockito.when(requestBody.contentLength()).thenReturn(43L);
        var mapper = new RetrofitRequestBodyMapper(100, Set.of());

        try (MockedStatic<RetrofitBodyMapperHelper> mockedStatic = Mockito.mockStatic(RetrofitBodyMapperHelper.class)) {
            mockedStatic.when(() -> RetrofitBodyMapperHelper.isProbablyUtf8(Mockito.isA(Buffer.class))).thenReturn(true);

            assertEquals("some data that may or may not be over limit", mapper.getBodyString(requestBody));
        }

        Mockito.verify(requestBody, Mockito.times(1)).writeTo(Mockito.isA(Buffer.class));
        Mockito.verify(requestBody, Mockito.times(1)).contentType();
        Mockito.verifyNoMoreInteractions(requestBody);
        Mockito.verifyNoInteractions(request, httpUrl, headers);
    }

    @Test
    @DisplayName("Exception is thrown when charset is null")
    void charsetIsNull() throws IOException {
        Mockito.doNothing().when(requestBody).writeTo(Mockito.isA(Buffer.class));
        Mockito.when(mediaType.charset(Mockito.any())).thenReturn(null);
        Mockito.when(requestBody.contentType()).thenReturn(mediaType);
        var mapper = new RetrofitRequestBodyMapper(100, Set.of());

        try (MockedStatic<RetrofitBodyMapperHelper> mockedStatic = Mockito.mockStatic(RetrofitBodyMapperHelper.class)) {
            mockedStatic.when(() -> RetrofitBodyMapperHelper.isProbablyUtf8(Mockito.isA(Buffer.class))).thenReturn(true);

            assertThrows(AssertionError.class, () -> mapper.getBodyString(requestBody));
        }

        Mockito.verify(requestBody, Mockito.times(1)).writeTo(Mockito.isA(Buffer.class));
        Mockito.verify(requestBody, Mockito.times(1)).contentType();
        Mockito.verifyNoMoreInteractions(requestBody);
        Mockito.verifyNoInteractions(request, httpUrl, headers);
    }

    @Test
    @DisplayName("Request content length is negative")
    void requestContentLengthIsNegative() throws IOException {
        Mockito.doAnswer(RetrofitRequestBodyMapperTest::setStringToBuffer).when(requestBody).writeTo(Mockito.isA(Buffer.class));
        Mockito.when(requestBody.contentType()).thenReturn(MediaType.get("text/plain"));
        Mockito.when(requestBody.contentLength()).thenReturn(-1L);
        var mapper = new RetrofitRequestBodyMapper(11, Set.of());

        try (MockedStatic<RetrofitBodyMapperHelper> mockedStatic = Mockito.mockStatic(RetrofitBodyMapperHelper.class)) {
            mockedStatic.when(() -> RetrofitBodyMapperHelper.isProbablyUtf8(Mockito.isA(Buffer.class))).thenReturn(true);

            assertEquals("some data t ... Content size: -1 characters", mapper.getBodyString(requestBody));
        }

        Mockito.verify(requestBody, Mockito.times(1)).writeTo(Mockito.isA(Buffer.class));
        Mockito.verifyNoMoreInteractions(requestBody);
        Mockito.verifyNoInteractions(request, httpUrl, headers);
    }

    @Test
    @DisplayName("Request content is limited")
    void requestContentIsLimited() throws IOException {
        var customBody = RequestBody.create("some data".getBytes(), MediaType.parse("text/plain"));
        var mapper = new RetrofitRequestBodyMapper(2, Set.of());

        assertEquals("so ... Content size: 9 characters", mapper.getBodyString(customBody));
    }

    @Test
    @DisplayName("Request content is returned in full")
    void requestContentIsReturnedInFull() throws MalformedURLException {
        var customBody = RequestBody.create("some data".getBytes(), MediaType.parse("text/plain"));
        Mockito.when(httpUrl.url()).thenReturn(new URL("http://localhost:6542/"));
        Mockito.when(request.url()).thenReturn(httpUrl);
        Mockito.when(request.headers()).thenReturn(headers);
        Mockito.when(request.body()).thenReturn(customBody);

        var mapper = new RetrofitRequestBodyMapper(9, Set.of());

        assertEquals("some data", mapper.getValue(request, null));

        Mockito.verify(request, Mockito.times(1)).url();
        Mockito.verify(request, Mockito.times(1)).headers();
        Mockito.verifyNoInteractions(requestBody);
    }

    @Test
    @DisplayName("Request content bytes count differs from character count")
    void requestContentBytesCountDiffersFromCharacterCount() throws IOException {
        // "õäöü" is 4 characters, but 8 bytes
        var customBody = RequestBody.create("õäöü", MediaType.parse("text/plain"));
        var mapper = new RetrofitRequestBodyMapper(5, Set.of());

        assertEquals("õäöü", mapper.getBodyString(customBody));
    }
}
