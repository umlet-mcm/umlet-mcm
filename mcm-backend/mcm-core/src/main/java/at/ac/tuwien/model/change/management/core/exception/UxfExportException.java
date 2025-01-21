package at.ac.tuwien.model.change.management.core.exception;

public class UxfExportException extends UxfRuntimeException {
    public UxfExportException(String message) {
        super(message);
    }

    public UxfExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
