package ee.bitweb.core.api;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;

import java.util.EnumSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldNameResolver {

    private static final String INDEX_OPEN = "[";
    private static final String INDEX_CLOSE = "]";
    private static final String FIELD_NAME_DELIMITER = ".";
    private static final EnumSet<ElementKind> IGNORED_ELEMENTS = EnumSet.of(ElementKind.METHOD, ElementKind.PARAMETER);

    public static String resolve(ConstraintViolation<?> error) {
        if (error instanceof ConstraintViolationImpl<?> violationImpl
                && violationImpl.getPropertyPath() instanceof PathImpl pathImpl) {

            return resolveFieldName(pathImpl);
        }

        return resolveWithRegex(error);
    }

    private static String resolveFieldName(PathImpl path) {
        StringBuilder builder = new StringBuilder();
        for (Path.Node node : path) {
            if (!(node instanceof NodeImpl nodeImpl) || IGNORED_ELEMENTS.contains(node.getKind())) {
                continue;
            }

            if (nodeImpl.isInIterable()) {
                builder.append(INDEX_OPEN);
                builder.append(nodeImpl.getIndex());
                builder.append(INDEX_CLOSE);
            }
            if (!builder.isEmpty()) {
                builder.append(FIELD_NAME_DELIMITER);
            }
            builder.append(nodeImpl.getName());
        }

        return builder.toString();
    }

    public static String resolveWithRegex(ConstraintViolation<?> error) {
        String[] parts = error.getPropertyPath().toString().split("\\.");

        return parts[parts.length - 1];
    }

}
