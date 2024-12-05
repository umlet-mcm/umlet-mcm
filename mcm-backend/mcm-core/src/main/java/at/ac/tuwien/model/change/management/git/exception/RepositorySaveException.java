package at.ac.tuwien.model.change.management.git.exception;

public class RepositorySaveException extends RepositoryAccessException {
    public RepositorySaveException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
