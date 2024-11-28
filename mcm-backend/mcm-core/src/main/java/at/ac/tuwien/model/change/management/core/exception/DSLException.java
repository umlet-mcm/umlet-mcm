package at.ac.tuwien.model.change.management.core.exception;

import java.io.IOException;

public class DSLException extends IOException {

    public DSLException(String message) {
        super(message);
    }

    public DSLException(String message, Throwable cause) {
        super(message, cause);
    }
}
