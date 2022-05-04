package ee.bitweb.core.retrofit.interceptor.auth.criteria;

import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;

import java.util.List;

@RequiredArgsConstructor
public class BlacklistCriteria implements AuthTokenCriteria {

    private final List<String> blacklist;

    @Override
    public boolean shouldApply(TokenProvider provider, Interceptor.Chain chain) {
        String url = chain.request().url().toString();

        for (String entry : blacklist) {
            if (url.contains(entry)) {
                return false;
            }
        }

        return true;
    }
}
