package ee.bitweb.core.scheduled;

import ee.bitweb.core.trace.invoker.scheduler.SchedulerTraceIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class ScheduledJob<T extends ScheduledRunnable> {

    private final T runnable;
    private final SchedulerTraceIdResolver traceIdResolver;

    public void run() {
        traceIdResolver.resolve();

        log.info("Started {}", getClass().getName());

        try {
            runnable.run();
        } catch (Exception e) {
           handleException(e);
        }

        log.info("Finished {}", getClass().getName());

        traceIdResolver.getContext().clear();
    }

    protected void handleException(Exception e) {
        log.error("{} failed: {}", getClass().getName(), e.getMessage(), e);
    }
}
