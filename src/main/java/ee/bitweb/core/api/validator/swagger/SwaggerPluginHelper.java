package ee.bitweb.core.api.validator.swagger;

public final class SwaggerPluginHelper {

    // Helper class, no need to create instance
    private SwaggerPluginHelper() {
    }

    public static String getPrefix(String description, String name) {
        String prefix = "";

        if (description != null && !description.isEmpty() && !description.equals(name)) {
            if (description.endsWith(".")) {
                prefix = " ";
            } else {
                prefix = ". ";
            }
        }

        return prefix;
    }
}
