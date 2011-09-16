package de.uni_koblenz.jgralab.greql2.exception;

public class SerialisingException extends RuntimeException {

	static final long serialVersionUID = 1;

	// the element which causes the error
	private Object value;

	public SerialisingException(String message, Object value) {
		super(message);
		this.value = value;
	}

	public SerialisingException(String message, Object value, Throwable cause) {
		super(message, cause);
		this.value = value;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + " (value was: " + value + ")";
	}
	
}
