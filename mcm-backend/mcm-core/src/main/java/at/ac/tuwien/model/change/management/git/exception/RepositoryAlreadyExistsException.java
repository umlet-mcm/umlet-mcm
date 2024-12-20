package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryAlreadyExistsException extends RepositoryAccessException{

    public RepositoryAlreadyExistsException(String msg) {
        super(msg);
    }
}
