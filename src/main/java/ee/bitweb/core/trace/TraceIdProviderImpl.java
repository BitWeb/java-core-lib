package ee.bitweb.core.trace;

import ee.bitweb.core.util.StringUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class TraceIdProviderImpl implements TraceIdProvider {

    private final TraceIdCustomizer customizer;

    public String generate(HttpServletRequest httpServletRequest) {
        String header = httpServletRequest.getHeader(customizer.getHeaderName());
        String trace = assemble();

        if (StringUtils.hasText(header)) {
            return header + customizer.getDelimiter() + trace;
        }

        return trace;
    }

    private String assemble() {
        if (customizer.getPrefix() == null) {
            return StringUtil.random(customizer.getLength());
        }

        return customizer.getPrefix() + StringUtil.random(customizer.getLength() - customizer.getPrefix().length());
    }
}
