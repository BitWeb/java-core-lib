package ee.bitweb.core.retrofit.logging.mappers;

import okhttp3.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class RetrofitResponseBodyMapperTest {

    @Test
    @DisplayName("Is redact url")
    void isRedactUrl() {
        var mapper = new RetrofitResponseBodyMapper(Set.of("https://www.google.com/"), 0);

        var response = new Response(
              request("GET"),
                Protocol.HTTP_1_0,
                "message",
                200,
                null,
                new Headers.Builder().build(),
                ResponseBody.create("123".getBytes(), MediaType.get("application/text")),
                null,
                null,
                null,
                1,
                2,
                null
        );

        var value = mapper.getValue(null, response);

        assertEquals("(body redacted)", value);
    }

    @Test
    @DisplayName("Promises body")
    void promisesBody() {
        var mapper = new RetrofitResponseBodyMapper(new HashSet<>(), 0);

        var response = new Response(
                request("HEAD"),
                Protocol.HTTP_1_0,
                "message",
                201,
                null,
                new Headers.Builder().build(),
                ResponseBody.create("123".getBytes(), MediaType.get("application/text")),
                null,
                null,
                null,
                1,
                2,
                null
        );

        var value = mapper.getValue(null, response);

        assertEquals("", value);
    }

    @Test
    @DisplayName("Body has unknown encoding")
    void bodyHasUnknownEncoding() {
        var mapper = new RetrofitResponseBodyMapper(new HashSet<>(), 0);

        var response = new Response(
                request("GET"),
                Protocol.HTTP_1_0,
                "message",
                201,
                null,
                new Headers.Builder().add("Content-Encoding", "unknownEncoding").build(),
                ResponseBody.create("123".getBytes(), MediaType.get("application/text")),
                null,
                null,
                null,
                1,
                2,
                null
        );

        var value = mapper.getValue(null, response);

        assertEquals("(encoded body omitted)", value);
    }

    @Test
    @DisplayName("Body missing")
    void bodyMissing() {
        var mapper = new RetrofitResponseBodyMapper(new HashSet<>(), 0);

        var response = new Response(
                request("GET"),
                Protocol.HTTP_1_0,
                "message",
                201,
                null,
                new Headers.Builder().build(),
                null,
                null,
                null,
                null,
                1,
                2,
                null
        );

        var value = mapper.getValue(null, response);

        assertEquals("(body missing)", value);
    }

    @Test
    @DisplayName("Response body is correctly returned")
    void bodyAvailable() {
        var mapper = new RetrofitResponseBodyMapper(new HashSet<>(), 4096);
        var response = new Response(
                request("GET"),
                Protocol.HTTP_2,
                "OK",
                200,
                null,
                new Headers.Builder().build(),
                ResponseBody.create("123".getBytes(), MediaType.get("application/text")),
                null,
                null,
                null,
                1,
                2,
                null
        );

        assertEquals("123", mapper.getValue(null, response));
    }

    @Test
    @DisplayName("Response body is correctly shortened")
    void bodyIsShortened() {
        var mapper = new RetrofitResponseBodyMapper(new HashSet<>(), 2);
        var response = new Response(
                request("GET"),
                Protocol.HTTP_2,
                "OK",
                200,
                null,
                new Headers.Builder().build(),
                ResponseBody.create("123".getBytes(), MediaType.get("application/text")),
                null,
                null,
                null,
                1,
                2,
                null
        );

        assertEquals("12 ... Content size: 3 characters", mapper.getValue(null, response));
    }

    @Test
    @DisplayName("Response body is correctly returned when empty")
    void bodyIsEmpty() {
        var mapper = new RetrofitResponseBodyMapper(new HashSet<>(), 4096);
        var response = new Response(
                request("GET"),
                Protocol.HTTP_2,
                "OK",
                200,
                null,
                new Headers.Builder().build(),
                ResponseBody.create(new byte[]{}, MediaType.get("application/text")),
                null,
                null,
                null,
                1,
                2,
                null
        );

        assertEquals("", mapper.getValue(null, response));
    }

    @Test
    @DisplayName("Response body is correctly returned when response is gzipped")
    void bodyIsGzipped() throws IOException {
        var mapper = new RetrofitResponseBodyMapper(new HashSet<>(), 4096);
        var response = new Response(
                request("GET"),
                Protocol.HTTP_2,
                "OK",
                200,
                null,
                new Headers.Builder().add("Content-Encoding", "gzip").build(),
                ResponseBody.create(gzip("some amount of data"), MediaType.get("application/text")),
                null,
                null,
                null,
                1,
                2,
                null
        );

        assertEquals("some amount of data", mapper.getValue(null, response));
    }

    @Test
    @DisplayName("Response is missing")
    void responseIsNull() {
        assertEquals("(response missing)", new RetrofitResponseBodyMapper(new HashSet<>(), 4096).getValue(null, null));
    }

    private Request request(String method) {
        return new Request(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("www.google.com")
                        .build(),
                method,
                new Headers.Builder().build(),
                RequestBody.create("123".getBytes(), MediaType.get("application/text")),
                new HashMap<>()
        );
    }

    private byte[] gzip(String data) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPOutputStream gzipOs = new GZIPOutputStream(os);
        byte[] buffer = data.getBytes();
        gzipOs.write(buffer, 0, buffer.length);
        gzipOs.close();

        return os.toByteArray();
    }
}
