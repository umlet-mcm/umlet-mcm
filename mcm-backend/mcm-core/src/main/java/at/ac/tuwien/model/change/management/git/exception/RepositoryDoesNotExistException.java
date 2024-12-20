package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryDoesNotExistException extends RepositoryAccessException {
    public RepositoryDoesNotExistException(String msg) {
        super(msg);
    }
}
