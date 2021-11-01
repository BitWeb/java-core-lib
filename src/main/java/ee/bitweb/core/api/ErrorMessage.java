package ee.bitweb.core.api;


public enum ErrorMessage {
    INVALID_ARGUMENT,
    CONTENT_TYPE_NOT_VALID,
    MESSAGE_NOT_READABLE,
    INTERNAL_SERVER_ERROR;

    public String toString() {
        return name();
    }
}
