package at.ac.tuwien.model.change.management.graphdb.exceptions;

public class InvalidQueryException extends RuntimeException {
    public InvalidQueryException(String message) {
        super(message);
    }
}
