package ee.bitweb.core.trace.thread;

import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class ThreadTraceIdResolver {

    private final TraceIdContext context;
    private final TraceIdCreator creator;

    public String resolve() {
        context.set(creator.generate(context.get()));

        return context.get();
    }
}
