package de.uni_koblenz.jgralab;

public class NoSuchAttributeException extends GraphException {

	private static final long serialVersionUID = 1L;

	public NoSuchAttributeException(String msg) {
		super(msg);
	}

	public NoSuchAttributeException(Throwable t) {
		super(t);
	}

	public NoSuchAttributeException(String msg, Throwable t) {
		super(msg, t);
	}
}
