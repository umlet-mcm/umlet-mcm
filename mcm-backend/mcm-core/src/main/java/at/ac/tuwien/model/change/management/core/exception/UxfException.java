package at.ac.tuwien.model.change.management.core.exception;

public class UxfException extends Exception{
    public UxfException(String message) {
        super(message);
    }

    public UxfException(String message, Throwable cause) {
        super(message, cause);
    }
}
