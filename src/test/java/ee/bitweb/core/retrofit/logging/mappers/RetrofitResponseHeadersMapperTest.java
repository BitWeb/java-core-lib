package ee.bitweb.core.retrofit.logging.mappers;

import okhttp3.Headers;
import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RetrofitResponseHeadersMapperTest {

    @Mock
    Response response;

    @Test
    @DisplayName("All headers are logged")
    void headersAreLogged() {
        Mockito.when(response.headers()).thenReturn(createHeaders());
        Mockito.when(response.body()).thenReturn(null);

        var mapper = new RetrofitResponseHeadersMapper(Set.of());

        assertEquals("X-Custom-Id: aBc134; Content-Length: 123; Content-Type: application/json", mapper.getValue(null, response));

        Mockito.verify(response, Mockito.times(1)).body();
        Mockito.verify(response, Mockito.times(1)).headers();
        Mockito.verifyNoMoreInteractions(response);
    }

    @Test
    @DisplayName("Suppressed headers list is honoured")
    void suppressedListIsHonoured() {
        Mockito.when(response.headers()).thenReturn(createHeaders());
        Mockito.when(response.body()).thenReturn(null);

        var mapper = new RetrofitResponseHeadersMapper(Set.of("X-Custom-Id"));

        assertEquals("X-Custom-Id:  ; Content-Length: 123; Content-Type: application/json", mapper.getValue(null, response));

        Mockito.verify(response, Mockito.times(1)).body();
        Mockito.verify(response, Mockito.times(1)).headers();
        Mockito.verifyNoMoreInteractions(response);
    }

    private Headers createHeaders() {
        Headers.Builder headersBuilder = new Headers.Builder();
        headersBuilder.add("Content-Type", "application/json");
        headersBuilder.add("Content-Length", "123");
        headersBuilder.add("X-Custom-Id", "aBc134");

        return headersBuilder.build();
    }
}
