package ee.bitweb.core.api;

public enum ErrorMessage {
    INVALID_ARGUMENT,
    CONTENT_TYPE_NOT_VALID,
    MESSAGE_NOT_READABLE,
    METHOD_NOT_ALLOWED,
    INTERNAL_SERVER_ERROR;

    @Override
    public String toString() {
        return name();
    }
}
