package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryReadException extends RepositoryAccessException {

    public RepositoryReadException(String msg) {
        super(msg);
    }

    public RepositoryReadException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
