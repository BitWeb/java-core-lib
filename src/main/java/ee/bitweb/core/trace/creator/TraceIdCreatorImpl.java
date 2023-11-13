package ee.bitweb.core.trace.creator;

import ee.bitweb.core.trace.TraceIdFormConfig;
import ee.bitweb.core.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class TraceIdCreatorImpl implements TraceIdCreator {

    private final TraceIdFormConfig config;

    public String generate(String traceId) {
        String trace = assemble();

        if (StringUtils.hasText(traceId)) {
            return traceId + config.getDelimiter() + trace;
        }

        return trace;
    }

    private String assemble() {
        if (config.getPrefix() == null) {
            return StringUtil.random(config.getLength());
        }

        return config.getPrefix() + StringUtil.random(config.getLength() - config.getPrefix().length());
    }
}
