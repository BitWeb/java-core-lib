package ee.bitweb.core.retrofit.logging;

import okhttp3.Interceptor;

public interface LoggingInterceptor extends Interceptor {

    LoggingInterceptor setLoggingLevel(LoggingLevel level);

    LoggingInterceptor setMaxLoggableRequestSize(int size);

    LoggingInterceptor setMaxLoggableResponseSize(int size);

    LoggingInterceptor addRedactBodyURL(String url);

    void redactHeader(String name);
}
