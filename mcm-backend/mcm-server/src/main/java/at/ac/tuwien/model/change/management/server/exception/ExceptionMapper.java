package at.ac.tuwien.model.change.management.server.exception;

import at.ac.tuwien.model.change.management.graphdb.exceptions.InvalidQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionMapper {
    @ExceptionHandler(InvalidQueryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String,String> requestHandlingInvalidQueryException(InvalidQueryException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Code", HttpStatus.BAD_REQUEST.toString());
        errors.put("Message", e.getMessage());
        return errors;

    }
}
