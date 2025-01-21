package at.ac.tuwien.model.change.management.core.exception;

public class UxfParsingException extends UxfRuntimeException {
    public UxfParsingException(String message) {
        super(message);
    }

    public UxfParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
