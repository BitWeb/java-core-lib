package ee.bitweb.core.validator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FileTypeEnum {
    CSV("csv", "text/csv"),
    PDF("pdf", "application/pdf"),
    PNG("png", "image/png"),
    JPEG("jpeg", "image/jpeg"),
    JPG("jpg", "image/jpg"),
    TXT("txt", "text/plain"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    ZIP("zip", "application/zip");

    private final String extension;
    private final String mime;
}
