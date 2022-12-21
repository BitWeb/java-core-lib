package ee.bitweb.core.audit.mappers;

import ee.bitweb.core.audit.AuditLogProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class ResponseBodyMapper extends AbstractAuditLogDataMapper {

    public static final String KEY = "response_body";

    private final AuditLogProperties properties;

    @Override
    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        String responseBody = "";

        ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;

        try {
            responseBody = new String(
                    responseWrapper.getContentInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            log.warn("Error occured while attempting to read response");
        }

        if (responseBody.length() > properties.getMaxLoggableResponseSize()) {
            responseBody = String.format("Content size: %s characters", responseBody.length());
        }

        return responseBody;
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
