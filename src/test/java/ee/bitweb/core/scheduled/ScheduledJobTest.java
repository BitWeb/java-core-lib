package ee.bitweb.core.scheduled;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ee.bitweb.core.trace.context.MDCTraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreatorImpl;
import ee.bitweb.core.trace.invoker.scheduler.SchedulerTraceIdFormConfig;
import ee.bitweb.core.trace.invoker.scheduler.SchedulerTraceIdResolver;
import ee.bitweb.core.utils.MemoryAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ScheduledJobTest {

    ch.qos.logback.classic.Logger logger;
    MemoryAppender memoryAppender;

    SchedulerTraceIdResolver schedulerTraceIdResolver;
    TestJob testJob;

    @Mock
    ScheduledRunnable scheduledRunnable;

    @BeforeEach
    void beforeEach() {
        MDC.clear();
        logger = (Logger) LoggerFactory.getLogger(ScheduledJob.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();

        schedulerTraceIdResolver = new SchedulerTraceIdResolver(
                MDCTraceIdContext.standard(),
                new TraceIdCreatorImpl(new SchedulerTraceIdFormConfig())
        );

        testJob = new TestJob(scheduledRunnable, schedulerTraceIdResolver);
    }

    @Test
    @DisplayName("Successful job should log correctly and clear MDC")
    void testSuccessfulJob() {
        // given
        doNothing().when(scheduledRunnable).run();

        // when
        testJob.runTestJob();

        // then
        verify(scheduledRunnable, times(1)).run();

        assertAll(
                () -> assertEquals(2, memoryAppender.getSize()),
                () -> assertEquals(1, memoryAppender.search("Started ee.bitweb.core.scheduled.ScheduledJobTest$TestJob", Level.INFO).size()),
                () -> assertEquals(1, memoryAppender.search("Finished ee.bitweb.core.scheduled.ScheduledJobTest$TestJob", Level.INFO).size()),
                () -> assertNull(MDC.getCopyOfContextMap())
        );
    }

    @Test
    @DisplayName("Unsuccessful job should log correctly and clear MDC")
    void testUnsuccessfulJob() {
        // given
        doThrow(RuntimeException.class).when(scheduledRunnable).run();

        // when
        testJob.runTestJob();

        // then
        verify(scheduledRunnable, times(1)).run();

        var errorMessages = memoryAppender.search("ee.bitweb.core.scheduled.ScheduledJobTest$TestJob failed: null", Level.ERROR);

        assertAll(
                () -> assertEquals(3, memoryAppender.getSize()),
                () -> assertEquals(1, memoryAppender.search("Started ee.bitweb.core.scheduled.ScheduledJobTest$TestJob", Level.INFO).size()),
                () -> assertEquals(1, errorMessages.size()),
                () -> assertEquals("java.lang.RuntimeException", errorMessages.get(0).getThrowableProxy().getClassName()), // validates that cause has been added to log message
                () -> assertEquals(1, memoryAppender.search("Finished ee.bitweb.core.scheduled.ScheduledJobTest$TestJob", Level.INFO).size()),
                () -> assertNull(MDC.getCopyOfContextMap())
        );
    }

    static class TestJob extends ScheduledJob<ScheduledRunnable> {

        public TestJob(ScheduledRunnable runnable, SchedulerTraceIdResolver traceIdResolver) {
            super(runnable, traceIdResolver);
        }

        public void runTestJob() {
            run();
        }
    }
}
