package ee.bitweb.core.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldNameResolver {

    private static final String INDEX_OPEN = "[";
    private static final String INDEX_CLOSE = "]";
    private static final String FIELD_NAME_DELIMITER = ".";

    public static String resolve(ConstraintViolation<?> error) {
        Path path = error.getPropertyPath();

        if (path == null) {
            return resolveWithRegex(error);
        }

        return resolveFieldName(path);
    }

    private static String resolveFieldName(Path path) {
        StringBuilder builder = new StringBuilder();
        String parameterName = null;

        for (Path.Node node : path) {
            switch (node.getKind()) {
                case PARAMETER -> parameterName = node.getName();
                case METHOD -> {
                    // Skip methods
                }
                default -> appendNode(builder, node);
            }
        }

        return builder.isEmpty() && parameterName != null ? parameterName : builder.toString();
    }

    private static void appendNode(StringBuilder builder, Path.Node node) {
        if (node.isInIterable()) {
            builder.append(INDEX_OPEN).append(node.getIndex()).append(INDEX_CLOSE);
        }
        if (!builder.isEmpty()) {
            builder.append(FIELD_NAME_DELIMITER);
        }
        builder.append(node.getName());
    }

    public static String resolveWithRegex(ConstraintViolation<?> error) {
        String[] parts = error.getPropertyPath().toString().split("\\.");

        return parts[parts.length - 1];
    }
}
