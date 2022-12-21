package ee.bitweb.core.retrofit.interceptor.auth.criteria;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import ee.bitweb.core.utils.MemoryAppender;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class WhitelistCriteriaTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private Interceptor.Chain chain;

    @Mock
    private Request request;

    @Mock
    private HttpUrl url;

    Logger logger;
    MemoryAppender memoryAppender;

    @BeforeEach
    void beforeEach() {
        logger = (Logger) LoggerFactory.getLogger(WhitelistCriteria.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    @DisplayName("Whitelist empty, must return false")
    void testShouldApplyReturnsFalseWhenWhitelistIsEmpty() {
        // given
        when(chain.request()).thenReturn(request);

        // when
        WhitelistCriteria criteria = new WhitelistCriteria(List.of());

        // then
        assertFalse(criteria.shouldApply(tokenProvider, chain));

        verifyNoMoreInteractions(chain);
        verifyNoInteractions(tokenProvider, request, url);
    }

    @Test
    @DisplayName("Only pattern matches, must return true")
    void testShouldApplyReturnsTrueWhenUrlDoesMatchesPattern() {
        // given
        when(chain.request()).thenReturn(request);
        when(request.url()).thenReturn(url);
        when(url.toString()).thenReturn("https://api.application.com/service/v1/entity/4163");

        // when
        WhitelistCriteria criteria = new WhitelistCriteria(List.of(
                Pattern.compile("^https://api.application.com/.*")
        ));

        // then
        assertTrue(criteria.shouldApply(tokenProvider, chain));

        verifyNoMoreInteractions(chain, request, url);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("No pattern matches, must return false")
    void testShouldApplyReturnsFalseWhenUrlDoesNotMatchPattern() {
        // given
        when(chain.request()).thenReturn(request);
        when(request.url()).thenReturn(url);
        when(url.toString()).thenReturn("https://api.application.com/service/v1/entity/4163");

        // when
        WhitelistCriteria criteria = new WhitelistCriteria(List.of(
                Pattern.compile("^https://secure.application.com/.*")
        ));

        // then
        assertFalse(criteria.shouldApply(tokenProvider, chain));

        verifyNoMoreInteractions(chain, request, url);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("One pattern matches, must return true")
    void testShouldApplyReturnsTrueWhenOnePatternMatches() {
        // given
        when(chain.request()).thenReturn(request);
        when(request.url()).thenReturn(url);
        when(url.toString()).thenReturn("https://api.application.com/service/v1/entity/4163");

        // when
        WhitelistCriteria criteria = new WhitelistCriteria(List.of(
                Pattern.compile("^https://api.application.com/.*"),
                Pattern.compile("^https://secure.application.com/.*")
        ));

        // then
        assertTrue(criteria.shouldApply(tokenProvider, chain));

        verifyNoMoreInteractions(chain, request, url);
        verifyNoInteractions(tokenProvider);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "^https://.*",
                    ".*api.application.com.*",
                    "^https://.*/some-api",
                    "^https://localhost.*",
            }
    )
    void testInvalidPatternResultsInErrorMessage(String pattern) {
        new WhitelistCriteria(List.of(Pattern.compile(pattern)));
        assertEquals(1, memoryAppender.getSize());
    }
}
