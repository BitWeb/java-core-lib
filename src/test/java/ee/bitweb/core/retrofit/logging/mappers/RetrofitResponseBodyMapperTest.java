package ee.bitweb.core.retrofit.logging.mappers;

import okhttp3.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

        Assertions.assertEquals("(body redacted)", value);
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

        Assertions.assertEquals("", value);
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

        Assertions.assertEquals("(encoded body omitted)", value);
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

        Assertions.assertEquals("(body missing)", value);
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
}
