package ee.bitweb.core.retrofit.builder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.logging.HttpLoggingInterceptor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LoggingLevel {

    NONE(HttpLoggingInterceptor.Level.NONE),
    BASIC(HttpLoggingInterceptor.Level.BASIC),
    HEADERS(HttpLoggingInterceptor.Level.HEADERS),
    BODY(HttpLoggingInterceptor.Level.BODY);

    @Getter(AccessLevel.PACKAGE)
    private HttpLoggingInterceptor.Level level;
}
