package ee.bitweb.core.retrofit.logging.mappers;

import okhttp3.Response;
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
class RetrofitResponseStatusCodeMapperTest {

    @Mock
    Response response;

    @Test
    @DisplayName("When response is not available, should return '-'")
    void testResponseIsNotAvailable() {
        RetrofitResponseStatusCodeMapper mapper = new RetrofitResponseStatusCodeMapper();

        assertEquals("-", mapper.getValue(null, null));

        Mockito.verifyNoInteractions(response);
    }

    @Test
    @DisplayName("Returns correct response code")
    void testResponseBodyLengthIsSuccessfullyRetrieved() {
        Mockito.when(response.code()).thenReturn(200);
        RetrofitResponseStatusCodeMapper mapper = new RetrofitResponseStatusCodeMapper();

        assertEquals("200", mapper.getValue(null, response));

        Mockito.verify(response, Mockito.times(1)).code();
        Mockito.verifyNoMoreInteractions(response);
    }
}
