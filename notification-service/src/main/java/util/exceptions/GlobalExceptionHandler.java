package util.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.reactive.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<?> handleServerWebInputException(ServerWebInputException ex){
        return ResponseEntity.status(400).body(
                new ExceptionModel(400, HttpStatus.BAD_REQUEST, ex.getReason())
        );
    }
    
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException ex){
        return ResponseEntity.status(400).body(
                new ExceptionModel(400, HttpStatus.BAD_REQUEST, ex.getMessage())
        );
    }
    
    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<?> handleNoDataFound(NoDataFoundException ex){
        return ResponseEntity.status(404).body(
                new ExceptionModel(404, HttpStatus.NOT_FOUND, ex.getMessage())
        );
    }
    
    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<?> handleMethodNotAllowedException(MethodNotAllowedException ex){
        return ResponseEntity.status(405).body(
                new ExceptionModel(405, HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage())
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex){
        return ResponseEntity.status(404).body(
                new ExceptionModel(404, HttpStatus.NOT_FOUND, ex.getMessage())
        );
    }
    
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<?> EntityAlreadyExistsException(EntityAlreadyExistsException ex){
        return ResponseEntity.status(409).body(
                new ExceptionModel(409, HttpStatus.CONFLICT, ex.getMessage())
        );
    }
    
    @ExceptionHandler(ForbidenActionException.class)
    public ResponseEntity<?> handleForbidenActionException(ForbidenActionException ex){
        return ResponseEntity.status(403).body(
                new ExceptionModel(403, HttpStatus.FORBIDDEN, ex.getMessage())
        );
    }
    
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<?> handleInvalidRequestException(InvalidRequestException ex){
        return ResponseEntity.status(400).body(
                new ExceptionModel(400, HttpStatus.BAD_REQUEST, ex.getMessage())
        );
    }
}