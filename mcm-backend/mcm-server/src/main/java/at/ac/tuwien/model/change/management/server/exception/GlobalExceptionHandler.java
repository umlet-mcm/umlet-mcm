package at.ac.tuwien.model.change.management.server.exception;

import at.ac.tuwien.model.change.management.git.exception.PersistenceException;
import at.ac.tuwien.model.change.management.git.exception.ConfigurationAlreadyExistsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private ResponseEntity<Object> defaultHandleExceptionInternal(Exception e, HttpStatus httpStatus, WebRequest request) {
        return handleExceptionInternal(e, new ApiError(e.getMessage()), new HttpHeaders(), httpStatus, request);
    }

    @ExceptionHandler(ConfigurationAlreadyExistsException.class)
    public ResponseEntity<Object> handleConfigurationAlreadyExistsException(ConfigurationAlreadyExistsException e, WebRequest request) {
        return defaultHandleExceptionInternal(e, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<Object> handlePersistenceException(PersistenceException e, WebRequest request) {
        return defaultHandleExceptionInternal(e, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
