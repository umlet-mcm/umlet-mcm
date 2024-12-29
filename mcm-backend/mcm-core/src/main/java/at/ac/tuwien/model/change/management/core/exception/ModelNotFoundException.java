package at.ac.tuwien.model.change.management.core.exception;

public class ModelNotFoundException extends Exception{
    public ModelNotFoundException(String message) {
        super(message);
    }

    public ModelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
