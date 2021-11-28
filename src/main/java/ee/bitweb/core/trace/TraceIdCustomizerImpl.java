package ee.bitweb.core.trace;

import lombok.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class TraceIdCustomizerImpl implements TraceIdCustomizer {

    private static final String DEFAULT_HEADER_NAME = "X-Trace-ID";
    private static final int DEFAULT_LENGTH = 20;
    private static final char DEFAULT_DELIMITER = '_';

    private final String headerName;
    private final String prefix;
    private final char delimiter;
    private final int length;
    private final List<AdditionalHeader> additionalHeaders;

    public static Builder builder() {
        return new Builder();
    }

    public static TraceIdCustomizerImpl standard() {
        return new TraceIdCustomizerImpl(
                DEFAULT_HEADER_NAME,
                null,
                DEFAULT_DELIMITER,
                DEFAULT_LENGTH,
                Collections.unmodifiableList(new ArrayList<>())
        );
    }

    private TraceIdCustomizerImpl(
            String headerName, String prefix, char delimiter, int length, List<AdditionalHeader> additionalHeaders
    ) {
        this.headerName = headerName;
        this.prefix = prefix;
        this.delimiter = delimiter;
        this.length = length;
        this.additionalHeaders = Collections.unmodifiableList(additionalHeaders);
    }

    @NoArgsConstructor
    public static class Builder {

        private String headerName = DEFAULT_HEADER_NAME;
        private String idPrefix;
        private char delimiter = DEFAULT_DELIMITER;
        private int length = DEFAULT_LENGTH;
        private List<AdditionalHeader> additionalHeaders = new ArrayList<>();

        public Builder headerName(String headerName) {
            if (headerName == null) {
                throw new IllegalArgumentException("Trace ID header name must not be null!");
            }

            if (StringUtils.containsWhitespace(headerName)) {
                throw new IllegalArgumentException("Trace ID header contain spaces!");
            }

            this.headerName = headerName;
            return this;
        }

        public Builder idPrefix(String idPrefix) {
            if (idPrefix != null) {
                if (idPrefix.length() > 4) {
                    throw new IllegalArgumentException("Trace ID prefix must be less than 4 characters!");
                }

                if (StringUtils.containsWhitespace(idPrefix)) {
                    throw new IllegalArgumentException("Trace ID prefix cannot contain spaces!");
                }
            }

            if (idPrefix != null && idPrefix.trim().isEmpty()) {
                this.idPrefix = null;
            } else {
                this.idPrefix = idPrefix;
            }

            return this;
        }

        public Builder delimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder length(int length) {
            if (length < 10 || length > 20) {
                throw new IllegalArgumentException("Trace ID length must be between 10 and 20!");
            }

            this.length = length;
            return this;
        }

        public Builder additionalHeader(String headerName) {
            return additionalHeader(headerName, headerName);
        }

        public Builder additionalHeader(String mdcKey, String headerName) {
            additionalHeaders.add(new AdditionalHeaderImpl(mdcKey, headerName));
            return this;
        }

        public Builder additionalHeaders(List<AdditionalHeader> additionalHeaders) {
            this.additionalHeaders = additionalHeaders;
            return this;
        }

        public TraceIdCustomizerImpl build() {
            return new TraceIdCustomizerImpl(headerName, idPrefix, delimiter, length, additionalHeaders);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class AdditionalHeaderImpl implements AdditionalHeader {
        private final String mdc;
        private final String header;
    }
}
