package ee.bitweb.core.retrofit.logging;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class NoopRetrofitLoggingInterceptorTest {

    @Mock
    Interceptor.Chain chain;

    @Mock
    Response response;

    @Mock
    Request request;

    @Test
    @DisplayName("Chain must proceed")
    void testChainProceeds() throws IOException {
        Mockito.when(chain.request()).thenReturn(request);
        Mockito.when(chain.proceed(request)).thenReturn(response);

        NoopRetrofitLoggingInterceptor interceptor = new NoopRetrofitLoggingInterceptor();
        interceptor.intercept(chain);

        Mockito.verify(chain, Mockito.times(1)).proceed(request);
        Mockito.verifyNoMoreInteractions(chain);
        Mockito.verifyNoInteractions(response, request);
    }
}
