package ee.bitweb.core.retrofit.logging.mappers;

import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RetrofitResponseBodySizeMapperTest {

    @Mock
    Response response;

    @Mock
    ResponseBody responseBody;

    @Test
    @DisplayName("Returns correct response size")
    void testResponseBodyLengthIsSuccessfullyRetrieved() {
        Mockito.when(response.body()).thenReturn(responseBody);
        Mockito.when(responseBody.contentLength()).thenReturn(1234L);
        RetrofitResponseBodySizeMapper mapper = new RetrofitResponseBodySizeMapper();

        assertEquals("1234", mapper.getValue(null, response));

        Mockito.verify(response, Mockito.times(1)).body();
        Mockito.verify(responseBody, Mockito.times(1)).contentLength();
        Mockito.verifyNoMoreInteractions(response, responseBody);
    }

    @Test
    @DisplayName("When response body is not available, should return '-'")
    void testResponseBodyIsNotAvailable() {
        Mockito.when(response.body()).thenReturn(null);
        RetrofitResponseBodySizeMapper mapper = new RetrofitResponseBodySizeMapper();

        assertEquals("-", mapper.getValue(null, response));

        Mockito.verify(response, Mockito.times(1)).body();
        Mockito.verifyNoMoreInteractions(response);
        Mockito.verifyNoInteractions(responseBody);
    }
}
