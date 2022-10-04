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
public class WhitelistCriteria implements AuthTokenCriteria {

    private final List<Pattern> whitelist;

    @Override
    public boolean shouldApply(TokenProvider provider, Interceptor.Chain chain) {
        log.trace("Checking if auth token should be added to request {}", chain.request());

        if (whitelist.isEmpty()) {
            log.debug("Rejected adding auth token to request because whitelist is empty");
            return false;
        }

        String url = chain.request().url().toString();

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
}
