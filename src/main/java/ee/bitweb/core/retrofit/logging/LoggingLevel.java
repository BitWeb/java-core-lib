package ee.bitweb.core.retrofit.logging;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LoggingLevel {

    NONE(0),
    BASIC(1),
    HEADERS(2),
    BODY(3);

    @Getter(AccessLevel.PACKAGE)
    private int level;
}
