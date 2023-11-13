package ee.bitweb.core.retrofit.interceptor.auth.criteria;

import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Getter
@ToString
@RequiredArgsConstructor
public class BlacklistCriteria implements AuthTokenCriteria {

    private final List<Pattern> blacklist;

    @Override
    public boolean shouldApply(TokenProvider provider, Interceptor.Chain chain) {
        log.trace("Checking if auth token should be added to request {}", chain.request());

        var url = chain.request().url().toString();

        if (blacklist.isEmpty()) {
            log.debug("Approved adding auth token to request for {} because blacklist is empty", url);
            return true;
        }

        for (Pattern entry : blacklist) {
            log.trace("Checking pattern {} against url {}", entry, url);

            if (entry.matcher(url).matches()) {
                log.debug("Rejected adding auth token to request for {}", url);
                return false;
            }
        }

        log.debug("Approved adding auth token to request for {}", url);

        return true;
    }
}
