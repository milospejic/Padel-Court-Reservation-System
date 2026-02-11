package util.exceptions;

public class ForbiddenActionException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ForbiddenActionException() {

	}

	public ForbiddenActionException(String message) {
		super(message);
	}

}