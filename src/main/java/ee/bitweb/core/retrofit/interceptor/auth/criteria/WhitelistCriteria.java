package ee.bitweb.core.retrofit.interceptor.auth.criteria;

import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import lombok.Getter;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Getter
@ToString
public class WhitelistCriteria implements AuthTokenCriteria {

    private static final List<String> invalidEndings = List.of("//.*");
    private static final List<String> invalidContainments = List.of(".*/", "^https://localhost.*");
    private static final List<String> validPrefixes = List.of("^http://", "^https://");

    private final List<Pattern> whitelist;

    public WhitelistCriteria(List<Pattern> whitelist) {
        this.whitelist = whitelist;
        validate();
    }

    @Override
    public boolean shouldApply(TokenProvider provider, Interceptor.Chain chain) {
        log.trace("Checking if auth token should be added to request {}", chain.request());

        if (whitelist.isEmpty()) {
            log.debug("Rejected adding auth token to request because whitelist is empty");
            return false;
        }

        var url = chain.request().url().toString();

        for (Pattern entry : whitelist) {
            log.trace("Checking pattern {} against url {}", entry, url);

            if (entry.matcher(url).matches()) {
                log.debug("Matched url {} against pattern {}", url, entry);

                return true;
            }
        }

        log.debug("Rejected adding auth token to request for {}", url);

        return false;
    }

    public void validate() {
        for (Pattern p : whitelist) {
            String pattern = p.toString();

            if (pattern.contains("*")) {
                assertPatternEndings(pattern);
                assertPatternContainment(pattern);
                assertStartsWithValidPrefix(pattern);
            }
        }
    }

    private void assertPatternEndings(String pattern) {
        for (String t : invalidEndings) {
            if (pattern.endsWith(t)) {
                log.error("Whitelist element {} should not end with {}", pattern, t);
            }
        }
    }

    private void assertPatternContainment(String pattern) {
        for (String t : invalidContainments) {
            if (pattern.contains(t)) {
                log.error("Whitelist element {} should not contain {}", pattern, t);
            }
        }
    }

    private void assertStartsWithValidPrefix(String pattern) {
        boolean valid = false;
        for (String s : validPrefixes) {
            valid = valid || pattern.startsWith(s);
        }

        if (!valid) {
            log.error("Whitelist element {} should start with any of  {}", pattern, validPrefixes);
        }
    }

}
