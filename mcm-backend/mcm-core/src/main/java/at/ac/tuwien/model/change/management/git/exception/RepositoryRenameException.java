package at.ac.tuwien.model.change.management.git.exception;

public class RepositoryRenameException extends RepositoryAccessException {

    public RepositoryRenameException(String msg) {
        super(msg);
    }

    public RepositoryRenameException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

