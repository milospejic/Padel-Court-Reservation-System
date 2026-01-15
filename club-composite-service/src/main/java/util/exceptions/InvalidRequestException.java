package util.exceptions;

public class InvalidRequestException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidRequestException() {

	}

	public InvalidRequestException(String message) {
		super(message);
	}

}