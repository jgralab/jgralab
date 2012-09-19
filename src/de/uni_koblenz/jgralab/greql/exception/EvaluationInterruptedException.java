package de.uni_koblenz.jgralab.greql.exception;

public class EvaluationInterruptedException extends GreqlException {
	private static final long serialVersionUID = -3667973824851303996L;

	public EvaluationInterruptedException() {
		super();
	}

	public EvaluationInterruptedException(String message) {
		super(message);
	}

	public EvaluationInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}
}
