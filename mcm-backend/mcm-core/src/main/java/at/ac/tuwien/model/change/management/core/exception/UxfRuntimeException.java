package at.ac.tuwien.model.change.management.core.exception;

public class UxfRuntimeException extends RuntimeException {
    public UxfRuntimeException(String message) {
        super(message);
    }

    public UxfRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
