package ee.bitweb.core.trace.invoker.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties("ee.bitweb.core.trace.invoker.http")
public class TraceIdFilterConfig {

    public static final String DEFAULT_HEADER_NAME = "X-Trace-ID";

    @NotBlank
    private String headerName = DEFAULT_HEADER_NAME;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdditionalHeader {

        @NotBlank
        private String contextKey;

        @NotBlank
        private String headerName;

        public AdditionalHeader(String headerName) {
            this(headerName, headerName);
        }
    }
}
