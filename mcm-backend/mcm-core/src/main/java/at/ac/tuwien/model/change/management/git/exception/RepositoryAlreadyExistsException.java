package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryAlreadyExistsException extends RepositoryCheckedException {

    public RepositoryAlreadyExistsException(String msg) {
        super(msg);
    }
}
