package ee.bitweb.core.retrofit.interceptor.auth.criteria;

import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;

import java.util.List;
import java.util.regex.Pattern;

@Getter
@RequiredArgsConstructor
public class BlacklistCriteria implements AuthTokenCriteria {

    private final List<Pattern> blacklist;

    @Override
    public boolean shouldApply(TokenProvider provider, Interceptor.Chain chain) {
        String url = chain.request().url().toString();

        for (Pattern entry : blacklist) {
            if (entry.matcher(url).matches()) {
                return false;
            }
        }

        return true;
    }
}
