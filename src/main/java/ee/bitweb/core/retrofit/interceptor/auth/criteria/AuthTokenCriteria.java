package ee.bitweb.core.retrofit.interceptor.auth.criteria;

import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import okhttp3.Interceptor;

public interface AuthTokenCriteria {

    boolean shouldApply(TokenProvider provider, Interceptor.Chain chain);
}
