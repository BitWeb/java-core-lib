package ee.bitweb.core.retrofit.logging.mappers;

import okhttp3.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Tag("unit")
class RetrofitRequestBodyMapperTest {

    @Test
    @DisplayName("Is redact url")
    void isRedactUrl() {
        var mapper = new RetrofitRequestBodyMapper(0, Set.of("https://www.google.com/"));

        var request = new Request(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("www.google.com")
                        .build(),
                "GET",
                new Headers.Builder().build(),
                RequestBody.create("123".getBytes(), MediaType.get("application/text")),
                new HashMap<>()
        );

        var value = mapper.getValue(request, null);

        Assertions.assertEquals("(body redacted)", value);
    }

    @Test
    @DisplayName("Body has unknown encoding")
    void bodyHasUnknownEncoding() {
        var mapper = new RetrofitRequestBodyMapper(0, new HashSet<>());

        var request = new Request(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("www.google.com")
                        .build(),
                "GET",
                new Headers.Builder().add("Content-Encoding", "unknownEncoding").build(),
                RequestBody.create("123".getBytes(), MediaType.get("application/text")),
                new HashMap<>()
        );

        var value = mapper.getValue(request, null);

        Assertions.assertEquals("(encoded body omitted)", value);
    }

    @Test
    @DisplayName("Max loggable request size")
    void maxLoggableRequestSize() {
        var mapper = new RetrofitRequestBodyMapper(2, new HashSet<>());

        var request = new Request(
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("www.google.com")
                        .build(),
                "GET",
                new Headers.Builder().build(),
                RequestBody.create("123".getBytes(), MediaType.get("application/text")),
                new HashMap<>()
        );

        var value = mapper.getValue(request, null);

        Assertions.assertEquals("12 ... Content size: 3 characters", value);
    }
}
