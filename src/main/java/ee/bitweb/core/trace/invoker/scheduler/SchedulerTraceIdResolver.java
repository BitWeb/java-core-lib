package ee.bitweb.core.trace.invoker.scheduler;

import ee.bitweb.core.trace.creator.TraceIdCreator;
import ee.bitweb.core.trace.context.TraceIdContext;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class SchedulerTraceIdResolver {

    private final TraceIdContext context;
    private final TraceIdCreator creator;

    public String resolve() {
        String existing = context.get();

        if (StringUtils.hasText(existing)) {
            throw new IllegalStateException("Context already has trace id populated, this is illegal.");
        }

        String traceId = creator.generate(null);
        context.set(traceId);

        return traceId;
    }
}
