package ee.bitweb.core.retrofit.interceptor.auth.criteria;

import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class BlacklistCriteriaTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private Interceptor.Chain chain;

    @Mock
    private Request request;

    @Mock
    private HttpUrl url;

    @Test
    @DisplayName("Blacklist empty, must return true")
    void testShouldApplyReturnsTrueWhenListIsEmpty() {
        // given
        when(chain.request()).thenReturn(request);
        when(request.url()).thenReturn(url);
        when(url.toString()).thenReturn("https://api.application.com/service/v1/entity/4163");

        // when
        BlacklistCriteria criteria = new BlacklistCriteria(List.of());

        // then
        assertTrue(criteria.shouldApply(tokenProvider, chain));

        verifyNoMoreInteractions(chain, request, url);
    }

    @Test
    @DisplayName("Only pattern matches, must return false")
    void testShouldApplyReturnsFalseWhenUrlDoesMatchesPattern() {
        // given
        when(chain.request()).thenReturn(request);
        when(request.url()).thenReturn(url);
        when(url.toString()).thenReturn("https://api.application.com/service/v1/entity/4163");

        // when
        BlacklistCriteria criteria = new BlacklistCriteria(List.of(
                Pattern.compile("^https://api.application.com/.*")
        ));

        // then
        assertFalse(criteria.shouldApply(tokenProvider, chain));

        verifyNoMoreInteractions(tokenProvider, chain, request, url);
    }

    @Test
    @DisplayName("No pattern matches, must return true")
    void testShouldApplyReturnsTrueWhenUrlDoesNotMatchPattern() {
        // given
        when(chain.request()).thenReturn(request);
        when(request.url()).thenReturn(url);
        when(url.toString()).thenReturn("https://api.application.com/service/v1/entity/4163");

        // when
        BlacklistCriteria criteria = new BlacklistCriteria(List.of(
                Pattern.compile("^https://secure.application.com/.*")
        ));

        // then
        assertTrue(criteria.shouldApply(tokenProvider, chain));

        verifyNoMoreInteractions(tokenProvider, chain, request, url);
    }

    @Test
    @DisplayName("One pattern matches, must return false")
    void testShouldApplyReturnsFalseWhenOnePatternMatches() {
        // given
        when(chain.request()).thenReturn(request);
        when(request.url()).thenReturn(url);
        when(url.toString()).thenReturn("https://api.application.com/service/v1/entity/4163");

        // when
        BlacklistCriteria criteria = new BlacklistCriteria(List.of(
                Pattern.compile("^https://api.application.com/.*"),
                Pattern.compile("^https://secure.application.com/.*")
        ));

        // then
        assertFalse(criteria.shouldApply(tokenProvider, chain));

        verifyNoMoreInteractions(tokenProvider, chain, request, url);
    }
}
