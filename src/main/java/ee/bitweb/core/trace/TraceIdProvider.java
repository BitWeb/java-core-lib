package ee.bitweb.core.trace;

import javax.servlet.http.HttpServletRequest;

public interface TraceIdProvider {

    String generate(HttpServletRequest request);
}
