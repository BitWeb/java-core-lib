package ee.bitweb.core.retrofit.logging.writers;

import ee.bitweb.core.exception.CoreException;
import ee.bitweb.core.retrofit.logging.RetrofitLoggingInterceptorImplementation;
import ee.bitweb.core.retrofit.logging.mappers.RetrofitRequestMethodMapper;
import ee.bitweb.core.retrofit.logging.mappers.RetrofitRequestUrlMapper;
import ee.bitweb.core.retrofit.logging.mappers.RetrofitResponseBodySizeMapper;
import ee.bitweb.core.retrofit.logging.mappers.RetrofitResponseStatusCodeMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

import java.util.Map;

@RequiredArgsConstructor
public class RetrofitLogLoggerWriterAdapter implements RetrofitLogWriteAdapter {

    public static final String LOGGER_NAME = "RetrofitLogger";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LOGGER_NAME);

    @Override
    public void write(Map<String, String> container) {
        Map<String, String> currentContext = MDC.getCopyOfContextMap();

        try {
            log(container);
        } finally {
            if (currentContext != null) {
                MDC.setContextMap(currentContext);
            } else {
                MDC.clear();
            }
        }
    }

    protected void log(Map<String, String> container) {
        container.forEach(MDC::put);

        if (!log.isInfoEnabled()) {
            logOrThrowError();
        } else {
            log.info(
                    "{} {} {} {}bytes {}ms",
                    get(container, RetrofitRequestMethodMapper.KEY),
                    get(container, RetrofitRequestUrlMapper.KEY),
                    get(container, RetrofitResponseStatusCodeMapper.KEY),
                    get(container, RetrofitResponseBodySizeMapper.KEY),
                    get(container, RetrofitLoggingInterceptorImplementation.DURATION_KEY)
            );
        }
    }

    protected void logOrThrowError() {
        String message = (
                "Retrofit interceptor has been enabled, but %s cannot write as log level does not permit INFO entries. This behaviour is strongly " +
                        "discouraged as the interceptor consumes resources for no real result. Please set property " +
                        "ee.bitweb.core.retrofit.logging-level=NONE if you wish to avoid this logging."
        ).formatted(RetrofitLogLoggerWriterAdapter.class.getSimpleName());

        if (log.isErrorEnabled()) {
            log.error(message);
        } else {
            throw new CoreException(message);
        }
    }

    protected String get(Map<String, String> container, String key) {
        if (container.containsKey(key)) {
            return container.get(key);
        }

        return "-";
    }
}
