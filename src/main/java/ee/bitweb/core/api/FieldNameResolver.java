package ee.bitweb.core.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldNameResolver {

    private static final String INDEX_OPEN = "[";
    private static final String INDEX_CLOSE = "]";
    private static final String FIELD_NAME_DELIMITER = ".";

    public static String resolve(ConstraintViolation<?> error) {
        if (error instanceof ConstraintViolationImpl<?> violationImpl
                && violationImpl.getPropertyPath() instanceof PathImpl pathImpl) {

            return resolveFieldName(pathImpl);
        }

        return resolveWithRegex(error);
    }

    private static String resolveFieldName(PathImpl path) {
        StringBuilder builder = new StringBuilder();
        String parameterName = null;

        for (Path.Node node : path) {
            if (!(node instanceof NodeImpl nodeImpl)) {
                continue;
            }

            switch (node.getKind()) {
                case PARAMETER -> parameterName = nodeImpl.getName();
                case METHOD -> {
                    // Skip methods
                }
                default -> appendNode(builder, nodeImpl);
            }
        }

        return builder.isEmpty() && parameterName != null ? parameterName : builder.toString();
    }

    private static void appendNode(StringBuilder builder, NodeImpl nodeImpl) {
        if (nodeImpl.isInIterable()) {
            builder.append(INDEX_OPEN).append(nodeImpl.getIndex()).append(INDEX_CLOSE);
        }
        if (!builder.isEmpty()) {
            builder.append(FIELD_NAME_DELIMITER);
        }
        builder.append(nodeImpl.getName());
    }

    public static String resolveWithRegex(ConstraintViolation<?> error) {
        String[] parts = error.getPropertyPath().toString().split("\\.");

        return parts[parts.length - 1];
    }

}
