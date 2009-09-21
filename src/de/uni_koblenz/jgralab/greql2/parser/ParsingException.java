package de.uni_koblenz.jgralab.greql2.parser;

public class ParsingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 894099164202915776L;

	private String message;

	private String tokenString;

	private int offset;

	private int length;

	public ParsingException(String msg, String token, int offset, int length) {
		super("Parsing error: " + msg + " at token '" + token + "' at position ("
				+ offset + "," + length + ")");
		message = msg;
		this.tokenString = token;
		;
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
		return message;
	}

	public String getToken() {
		return tokenString;
	}

}
