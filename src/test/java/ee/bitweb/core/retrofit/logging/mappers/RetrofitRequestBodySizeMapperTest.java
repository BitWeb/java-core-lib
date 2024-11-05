package ee.bitweb.core.retrofit.logging.mappers;

import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RetrofitRequestBodySizeMapperTest {

    @Mock
    Request request;

    @Mock
    RequestBody requestBody;

    @Test
    @DisplayName("Returns correct request size")
    void testRequestBodyLengthIsSuccessfullyRetrieved() throws IOException {
        Mockito.when(request.body()).thenReturn(requestBody);
        Mockito.when(requestBody.contentLength()).thenReturn(123L);
        RetrofitRequestBodySizeMapper mapper = new RetrofitRequestBodySizeMapper();

        assertEquals("123", mapper.getValue(request, null));

        Mockito.verify(request, Mockito.times(1)).body();
        Mockito.verify(requestBody, Mockito.times(1)).contentLength();
        Mockito.verifyNoMoreInteractions(request, requestBody);
    }

    @Test
    @DisplayName("When request body is not available, should return '-'")
    void testRequestBodyIsNotAvailable() {
        Mockito.when(request.body()).thenReturn(null);
        RetrofitRequestBodySizeMapper mapper = new RetrofitRequestBodySizeMapper();

        assertEquals("-", mapper.getValue(request, null));

        Mockito.verify(request, Mockito.times(1)).body();
        Mockito.verifyNoMoreInteractions(request);
        Mockito.verifyNoInteractions(requestBody);
    }

    @Test
    @DisplayName("When request body content size throws exception, should return 'Parse error.'")
    void testRequestBodySizeThrowsException() throws IOException {
        Mockito.when(request.body()).thenReturn(requestBody);
        Mockito.when(requestBody.contentLength()).thenThrow(IOException.class);
        RetrofitRequestBodySizeMapper mapper = new RetrofitRequestBodySizeMapper();

        assertEquals("Parse error.", mapper.getValue(request, null));

        Mockito.verify(request, Mockito.times(1)).body();
        Mockito.verify(requestBody, Mockito.times(1)).contentLength();
        Mockito.verifyNoMoreInteractions(request, requestBody);
    }
}
