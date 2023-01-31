package ee.bitweb.core.audit.testcomponent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Slf4j
@Validated
@RestController
@RequestMapping(AuditLogController.BASE_URL)
@RequiredArgsConstructor
public class AuditLogController {

    public static final String BASE_URL = "/audit";

    @GetMapping("/validated")
    public SimpleValidatedObject getValidated(@Valid SimpleValidatedObject data) {

        return data;
    }

    @GetMapping("/ignored")
    public void ignored() {

    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SimpleValidatedObject {

        @NotNull
        @NotBlank
        private String simpleProperty;
    }
}
