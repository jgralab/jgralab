package de.uni_koblenz.jgralab.eca;

public class ECAException extends RuntimeException {

	private static final long serialVersionUID = 6680822701697571660L;

	public ECAException() {
	}

	public ECAException(String msg) {
		super(msg);
	}

	public ECAException(Throwable t) {
		super(t);
	}

	public ECAException(String msg, Throwable t) {
		super(msg, t);
	}
}
