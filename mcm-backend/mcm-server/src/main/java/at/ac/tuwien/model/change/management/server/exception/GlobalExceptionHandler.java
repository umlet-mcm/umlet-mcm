package at.ac.tuwien.model.change.management.server.exception;

import at.ac.tuwien.model.change.management.core.exception.*;
import at.ac.tuwien.model.change.management.core.service.ConfigurationNotFoundException;
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

    @ExceptionHandler(ConfigurationDoesNotExistException.class)
    public ResponseEntity<Object> handleConfigurationDoesNotExistException(ConfigurationDoesNotExistException e, WebRequest request) {
        return defaultHandleExceptionInternal(e, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<Object> handleConfigurationException(ConfigurationException e, WebRequest request) {
        return defaultHandleExceptionInternal(e, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleConfigurationNotFoundException(ConfigurationNotFoundException e, WebRequest request) {
        return defaultHandleExceptionInternal(e, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ConfigurationVersionMismatchException.class)
    public ResponseEntity<Object> handleConfigurationVersionMismatchException(ConfigurationVersionMismatchException e, WebRequest request) {
        return defaultHandleExceptionInternal(e, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleConfigurationValidationException(ConfigurationValidationException e, WebRequest request) {
        return defaultHandleExceptionInternal(e, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConfigurationVersionDoesNotExistException.class)
    public ResponseEntity<Object> handleConfigurationVersionDoesNotExistException(ConfigurationVersionDoesNotExistException e, WebRequest request) {
        return defaultHandleExceptionInternal(e, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleModelNotFoundException(ModelNotFoundException e, WebRequest request) {
        return defaultHandleExceptionInternal(e, HttpStatus.NOT_FOUND, request);
    }
}
