package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryWriteException extends RepositoryAccessException {

    public RepositoryWriteException(String msg) {
        super(msg);
    }

    public RepositoryWriteException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
