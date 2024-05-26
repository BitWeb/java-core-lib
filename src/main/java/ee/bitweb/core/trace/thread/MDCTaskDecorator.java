package ee.bitweb.core.trace.thread;

import ee.bitweb.core.trace.thread.decorator.SecurityAwareMDCTaskDecorator;

/**
 * @deprecated use BasicMDCTaskDecorator or SecurityAwareMDCTaskDecorator
 */
@Deprecated(since = "3.3.0", forRemoval = true)
public class MDCTaskDecorator extends SecurityAwareMDCTaskDecorator {

    public MDCTaskDecorator(ThreadTraceIdResolver resolver) {
        super(resolver);
    }
}
