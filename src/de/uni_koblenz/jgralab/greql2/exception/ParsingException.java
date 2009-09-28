package de.uni_koblenz.jgralab.greql2.exception;

public class ParsingException extends Greql2Exception {

	private static final long serialVersionUID = 894099164202915776L;

	private String errorMessage;

	private String tokenString;

	private int offset;

	private int length;

	public ParsingException(String msg, String token, int offset, int length) {
		super("Parsing error: " + msg + " at token '" + token
				+ "' at position (" + offset + "," + length + ")");
		errorMessage = msg;
		this.tokenString = token;
		this.offset = offset;
		this.length = length;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getToken() {
		return tokenString;
	}

}
