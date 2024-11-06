package at.ac.tuwien.model.change.management.git.exception;



public class PersistenceException extends RuntimeException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    @SuppressWarnings("unused")
    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
