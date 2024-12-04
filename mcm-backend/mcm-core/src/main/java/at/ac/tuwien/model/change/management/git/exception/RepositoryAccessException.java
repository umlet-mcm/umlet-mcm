package at.ac.tuwien.model.change.management.git.exception;

import org.springframework.dao.DataAccessException;

public class RepositoryAccessException extends DataAccessException {

    public RepositoryAccessException(String msg) {
        super(msg);
    }

    public RepositoryAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
