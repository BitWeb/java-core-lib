package ee.bitweb.core.retrofit.logging;

import okhttp3.Interceptor;

public interface LoggingInterceptor extends Interceptor {

    LoggingInterceptor setLevel(LoggingLevel level);
    void redactHeader(String name);
}
