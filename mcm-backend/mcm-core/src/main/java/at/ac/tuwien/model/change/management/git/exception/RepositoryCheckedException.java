package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryCheckedException extends Exception {
    public RepositoryCheckedException(String msg) {
        super(msg);
    }

    public RepositoryCheckedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
