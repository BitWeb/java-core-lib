package ee.bitweb.core.retrofit.logging.writers;

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

        log(container);

        if (currentContext != null) {
            MDC.setContextMap(currentContext);
        }
    }

    private void log(Map<String, String> container) {
        container.forEach(MDC::put);

        log.info(
                "Method({}), URL({}) Status({}) ResponseSize({}) Duration({} ms)",
                get(container, RetrofitRequestMethodMapper.KEY),
                get(container, RetrofitRequestUrlMapper.KEY),
                get(container, RetrofitResponseStatusCodeMapper.KEY),
                get(container, RetrofitResponseBodySizeMapper.KEY),
                get(container, RetrofitLoggingInterceptorImplementation.DURATION_KEY)
        );
    }

    private String get(Map<String, String> container, String key) {
        if (container.containsKey(key)) {
            return container.get(key);
        }

        return "-";
    }
}
