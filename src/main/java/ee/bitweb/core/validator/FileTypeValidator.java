package ee.bitweb.core.validator;


import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {

    private FileTypeEnum[] allowedTypes = {};

    @Override
    public void initialize(FileType fileType) {
        this.allowedTypes = fileType.value();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null) return true;

        String mime = file.getContentType();
        String name = file.getOriginalFilename();
        String extension = getExtension(name);

        for (FileTypeEnum type : allowedTypes) {
            if (type.getExtension().equals(extension) && type.getMime().equals(mime)) {
                return true;
            }
        }

        return false;
    }

    private String getExtension(String name) {
        if (name == null) return null;

        int i = name.lastIndexOf('.');
        if (i == -1) return null;

        return name.substring(i + 1).toLowerCase();
    }
}
