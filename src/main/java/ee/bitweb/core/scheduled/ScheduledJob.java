package ee.bitweb.core.scheduled;

import ee.bitweb.core.trace.invoker.scheduler.SchedulerTraceIdResolver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ScheduledJob<T extends ScheduledRunnable> {

    private T runnable;
    private SchedulerTraceIdResolver traceIdResolver;

    public void run() {
        traceIdResolver.resolve();

        log.info("Started {}", getClass().getName());

        try {
            runnable.run();
        } catch (Exception e) {
            log.error("{} failed: {}", getClass().getName(), e.getMessage(), e);
        }

        log.info("Finished {}", getClass().getName());

        traceIdResolver.getContext().clear();
    }
}
