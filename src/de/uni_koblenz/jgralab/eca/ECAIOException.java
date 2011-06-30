package de.uni_koblenz.jgralab.eca;

//Exception
public class ECAIOException extends Exception {
	private static final long serialVersionUID = 4569564712278582919L;

	public ECAIOException() {
	}

	public ECAIOException(String msg) {
		super(msg);
	}

	public ECAIOException(Throwable t) {
		super(t);
	}

	public ECAIOException(String msg, Throwable t) {
		super(msg, t);
	}
}