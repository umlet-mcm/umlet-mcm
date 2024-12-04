package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryDeleteException extends RepositoryAccessException {
    public RepositoryDeleteException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
