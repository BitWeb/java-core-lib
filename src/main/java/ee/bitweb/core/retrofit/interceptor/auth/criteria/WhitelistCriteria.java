package ee.bitweb.core.retrofit.interceptor.auth.criteria;

import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import java.util.List;

@RequiredArgsConstructor
public class WhitelistCriteria implements AuthTokenCriteria {

    private final List<String> whitelist;

    @Override
    public boolean shouldApply(TokenProvider provider, Interceptor.Chain chain) {
        String url = chain.request().url().toString();

        for (String entry : whitelist) {
            if (url.contains(entry)) {
                return true;
            }
        }

        return false;
    }
}
