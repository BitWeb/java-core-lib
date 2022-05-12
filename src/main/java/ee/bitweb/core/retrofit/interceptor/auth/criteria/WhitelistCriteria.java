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
        String url = chain.request().url().toString();

        for (Pattern entry : whitelist) {
            if (entry.matcher(url).matches()) {
                return true;
            }
        }

        log.debug("Rejected adding auth token to request for {}", url);

        return false;
    }
}
