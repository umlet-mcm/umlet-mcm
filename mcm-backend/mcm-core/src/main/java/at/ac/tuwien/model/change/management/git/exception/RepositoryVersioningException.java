package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryVersioningException extends RepositoryAccessException{

    public RepositoryVersioningException(String message) {
        super(message);
    }

    public RepositoryVersioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
