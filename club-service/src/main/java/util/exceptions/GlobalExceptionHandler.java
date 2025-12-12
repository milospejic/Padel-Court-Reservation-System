package util.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<?> handleMissingParameters(MissingServletRequestParameterException ex){
		return ResponseEntity.status(400).body(
				new ExceptionModel(400, HttpStatus.BAD_REQUEST, ex.getMessage())
				);
	}
	
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<?> handleNullPointerException(NullPointerException ex){
		return ResponseEntity.status(400).body(
				new ExceptionModel(400, HttpStatus.BAD_REQUEST, ex.getMessage())
				);
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> HttpMessageNotReadableException(HttpMessageNotReadableException ex){
		return ResponseEntity.status(400).body(
				new ExceptionModel(400, HttpStatus.BAD_REQUEST, ex.getMessage())
				);
	}
	
	@ExceptionHandler(NoDataFoundException.class)
	public ResponseEntity<?> handleNoDataFound(NoDataFoundException ex){
		return ResponseEntity.status(404).body(
				new ExceptionModel(404,HttpStatus.NOT_FOUND, ex.getMessage())
				);
	}
	
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex){
		return ResponseEntity.status(405).body(
				new ExceptionModel(405,HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage())
				);
	}

	
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex){
		return ResponseEntity.status(404).body(
				new ExceptionModel(400,HttpStatus.NOT_FOUND, ex.getMessage())
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