package ee.bitweb.core.util;

import ee.bitweb.core.exception.InvalidArgumentException;
import ee.bitweb.core.util.HttpForwardedHeaderParser.ForwardedHeader;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.simplify4u.slf4jmock.LoggerMock;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class HttpForwardedHeaderParserTest {

    Logger logger;

    @BeforeEach
    void setUp() {
        logger = Mockito.mock(Logger.class);
        LoggerMock.setMock(HttpForwardedHeaderParser.class, logger);
    }

    @AfterEach
    void tearDown() {
        LoggerMock.clearMock(HttpForwardedHeaderParser.class);
    }

    @Test
    @DisplayName("Given null string should throw exception")
    void testParseThrowsWithNullString() {
        InvalidArgumentException e = assertThrows(
                InvalidArgumentException.class,
                () -> HttpForwardedHeaderParser.parse(((String) null)),
                "expected InvalidArgumentException to be thrown"
        );

        assertEquals("header is required", e.getMessage());
    }

    @Test
    @DisplayName("Given empty string should throw exception")
    void testParseThrowsWithEmptyString() {
        InvalidArgumentException e = assertThrows(
                InvalidArgumentException.class,
                () -> HttpForwardedHeaderParser.parse(((String) null)),
                "expected InvalidArgumentException to be thrown"
        );

        assertEquals("header is required", e.getMessage());
    }

    @Test
    @DisplayName("Given invalid parameter should skip the parameter and log it")
    void testInvalidParameter() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "for=_hidden, for=_SEVKISEK=2"
        );

        assertAll(
                () -> assertEquals(0, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals(1, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("_hidden", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals(0, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals(0, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );

        Mockito.verify(logger).debug("'{}' is not recognisable", "for=_SEVKISEK=2");
        Mockito.verifyNoMoreInteractions(logger);
    }

    @Test
    @DisplayName("Given 'for=12.34.56.78;host=example.com;proto=https, for=23.45.67.89' should correctly parse header value")
    void testParseHeader1() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "for=12.34.56.78;host=example.com;proto=https, for=23.45.67.89"
        );

        assertAll(
                () -> assertEquals(0, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals(2, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("12.34.56.78", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals("23.45.67.89", result.getAFor().get(1), "'for[1]' is not correct"),
                () -> assertEquals(1, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals("example.com", result.getHost().get(0), "'host[0]' is not correct"),
                () -> assertEquals(1, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals("https", result.getProto().get(0), "'proto[0]' is not correct"),
                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );
    }

    @Test
    @DisplayName("Given 'for=12.34.56.78, for=23.45.67.89;secret=egah2CGj55fSJFs, for=10.1.2.3' should correctly parse header value")
    void testParseHeader2() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "for=12.34.56.78, for=23.45.67.89;secret=egah2CGj55fSJFs, for=10.1.2.3"
        );

        assertAll(
                () -> assertEquals(0, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals(3, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("12.34.56.78", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals("23.45.67.89", result.getAFor().get(1), "'for[1]' is not correct"),
                () -> assertEquals("10.1.2.3", result.getAFor().get(2), "'for[2]' is not correct"),
                () -> assertEquals(0, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals(0, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals(1, result.getExtensions().size(), "'extensions' does not have correct number of elements"),
                () -> assertEquals("secret", result.getExtensions().get(0).getKey(), "'extensions[0]' key is not correct"),
                () -> assertEquals("egah2CGj55fSJFs", result.getExtensions().get(0).getValue(), "'extensions[0]' value is not correct"),
                () -> assertEquals("secret=egah2CGj55fSJFs", result.getExtensions().get(0).toString(), "'extensions[0]' is not correct")
        );
    }

    @Test
    @DisplayName("Given 'For=\"[2001:db8:cafe::17]:4711\"' should correctly parse header value")
    void testParseHeader3() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "For=\"[2001:db8:cafe::17]:4711\""
        );

        assertAll(
                () -> assertEquals(0, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals(1, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("[2001:db8:cafe::17]:4711", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals(0, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals(0, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );
    }

    @Test
    @DisplayName("Given 'for=192.0.2.60;proto=http;by=203.0.113.43' should correctly parse header value")
    void testParseHeader4() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "for=192.0.2.60;proto=http;by=203.0.113.43"
        );

        assertAll(
                () -> assertEquals(1, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals("203.0.113.43", result.getBy().get(0), "'by[0] is not correct"),
                () -> assertEquals(1, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("192.0.2.60", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals(0, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals(1, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals("http", result.getProto().get(0), "'proto[0]' is not correct"),
                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );
    }

    @Test
    @DisplayName("Given 'for=\"_gazonk\"' should correctly parse header value")
    void testParseHeader5() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "for=\"_gazonk\""
        );

        assertAll(
                () -> assertEquals(0, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals(1, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("_gazonk", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals(0, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals(0, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );
    }

    @Test
    @DisplayName("Given 'for=_hidden, for=_SEVKISEK' should correctly parse header value")
    void testParseHeader6() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "for=_hidden, for=_SEVKISEK"
        );

        assertAll(
                () -> assertEquals(0, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals(2, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("_hidden", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals("_SEVKISEK", result.getAFor().get(1), "'for[1]' is not correct"),
                () -> assertEquals(0, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals(0, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );
    }

    @Test
    @DisplayName("Given 'for=192.0.2.43, for=\"[2001:db8:cafe::17]\", for=unknown' should correctly parse header value")
    void testParseHeader7() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "for=192.0.2.43, for=\"[2001:db8:cafe::17]\", for=unknown"
        );

        assertAll(
                () -> assertEquals(0, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals(3, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("192.0.2.43", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals("[2001:db8:cafe::17]", result.getAFor().get(1), "'for[1]' is not correct"),
                () -> assertEquals("unknown", result.getAFor().get(2), "'for[2]' is not correct"),
                () -> assertEquals(0, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals(0, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );
    }

    @Test
    @DisplayName("Given 'for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com' should correctly parse header value")
    void testParseHeader8() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com"
        );

        assertAll(
                () -> assertEquals(1, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals("203.0.113.60", result.getBy().get(0), "'by[0] is not correct"),
                () -> assertEquals(2, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("192.0.2.43", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals("198.51.100.17", result.getAFor().get(1), "'for[1]' is not correct"),
                () -> assertEquals(1, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals("example.com", result.getHost().get(0), "'host[0]' is not correct"),
                () -> assertEquals(1, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals("http", result.getProto().get(0), "'proto[0]' is not correct"),
                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );
    }

    @Test
    @DisplayName("Given 'proto=https;host=\"localhost:5001\";for=\"[::1]:20173\";by=_YQuN68tm6' should correctly parse header value")
    void testParseHeader9() {
        ForwardedHeader result = HttpForwardedHeaderParser.parse(
                "proto=https;host=\"localhost:5001\";for=\"[::1]:20173\";by=_YQuN68tm6"
        );

        assertAll(
                () -> assertEquals(1, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals("_YQuN68tm6", result.getBy().get(0), "'by[0] is not correct"),
                () -> assertEquals(1, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("[::1]:20173", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals(1, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals("localhost:5001", result.getHost().get(0), "'host[0]' is not correct"),
                () -> assertEquals(1, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals("https", result.getProto().get(0), "'proto[0]' is not correct"),
                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );
    }

    @Test
    @DisplayName("Given two headers should parse them correctly and merge values")
    void testParseWithEnumeration() {

        ForwardedHeader result = HttpForwardedHeaderParser.parse(Collections.enumeration(List.of(
                "proto=https;host=\"localhost:5001\";for=\"[::1]:20173\";by=_YQuN68tm6",
                "for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com"
        )));

        assertAll(
                () -> assertEquals(2, result.getBy().size(), "'by' does not have correct number of elements"),
                () -> assertEquals("_YQuN68tm6", result.getBy().get(0), "'by[0] is not correct"),
                () -> assertEquals("203.0.113.60", result.getBy().get(1), "'by[1] is not correct"),

                () -> assertEquals(3, result.getAFor().size(), "'for' does not have correct number of elements"),
                () -> assertEquals("[::1]:20173", result.getAFor().get(0), "'for[0]' is not correct"),
                () -> assertEquals("192.0.2.43", result.getAFor().get(1), "'for[1]' is not correct"),
                () -> assertEquals("198.51.100.17", result.getAFor().get(2), "'for[2]' is not correct"),

                () -> assertEquals(2, result.getHost().size(), "'host' does not have correct number of elements"),
                () -> assertEquals("localhost:5001", result.getHost().get(0), "'host[0]' is not correct"),
                () -> assertEquals("example.com", result.getHost().get(1), "'host[1]' is not correct"),

                () -> assertEquals(2, result.getProto().size(), "'proto' does not have correct number of elements"),
                () -> assertEquals("https", result.getProto().get(0), "'proto[0]' is not correct"),
                () -> assertEquals("http", result.getProto().get(1), "'proto[1]' is not correct"),

                () -> assertEquals(0, result.getExtensions().size(), "'extensions' does not have correct number of elements")
        );
    }
}
