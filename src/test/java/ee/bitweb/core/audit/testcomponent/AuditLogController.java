package ee.bitweb.core.audit.testcomponent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Slf4j
@Validated
@RestController
@RequestMapping(AuditLogController.BASE_URL)
@RequiredArgsConstructor
public class AuditLogController {

    public static final String BASE_URL = "/audit";

    @PostMapping("/validated")
    public SimpleValidatedObject getValidated(@RequestBody @Valid SimpleValidatedObject data) {

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
