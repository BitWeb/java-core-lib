package ee.bitweb.core.validator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class FileTypeValidatorUnitTest {

    FileTypeValidator validator = new FileTypeValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "application/pdf",
            "image/png",
            "image/jpeg",
    })
    void onInvalidMimeShouldReturnFalse(String mime) throws Exception {
        validator.initialize(getAnnotation("image"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",
                mime,
                new byte[0]
        );

        assertFalse(validator.isValid(file, null));
    }

    @Test
    void onInvalidExtensionShouldReturnFalse() throws Exception {
        validator.initialize(getAnnotation("image"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",
                "image/png",
                new byte[0]
        );

        assertFalse(validator.isValid(file, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "my-file.txt",
            "my-file.png",
            "my-file.jpg",
            "my-file.jpeg"
    })
    void onInvalidExtensionAndMimeShouldReturnFalse(String fileName) throws Exception {
        validator.initialize(getAnnotation("excel"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                "application/pdf",
                new byte[0]
        );

        assertFalse(validator.isValid(file, null));
    }

    @Test
    void onValidMimeAndExtensionShouldReturnTrue() throws Exception {
        validator.initialize(getAnnotation("image"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.png",
                "image/png",
                new byte[0]
        );

        assertTrue(validator.isValid(file, null));
    }

    private FileType getAnnotation(String field) throws Exception {
        return Validator.class.getDeclaredField(field).getAnnotation(FileType.class);
    }

    @SuppressWarnings("unused")
    private static class Validator {

        @FileType(FileTypeEnum.XLSX)
        private String excel;

        @FileType({FileTypeEnum.PNG, FileTypeEnum.JPEG, FileTypeEnum.JPG})
        private String image;
    }
}
