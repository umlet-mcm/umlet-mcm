package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryCreateException extends RepositoryAccessException{
    public RepositoryCreateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
