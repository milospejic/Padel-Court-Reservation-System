package util.exceptions;

public class ForbidenActionException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ForbidenActionException() {

	}

	public ForbidenActionException(String message) {
		super(message);
	}

}