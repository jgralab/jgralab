package de.uni_koblenz.jgralab.greql2.exception;

public class ParsingException extends Greql2Exception {

	private static final long serialVersionUID = 894099164202915776L;

	private String errorMessage;

	private String tokenString;

	private int offset;

	private int length;

	public ParsingException(String msg, String token, int offset, int length,
			String query) {
		super("Parsing error: " + msg + " at token '" + token
				+ "' at position (" + offset + "," + length + "): '"
				+ surrounding(query, offset, length) + "'");
		errorMessage = msg;
		this.tokenString = token;
		this.offset = offset;
		this.length = length;
	}

	private static String surrounding(String query, int offset, int length) {
		int s = offset - 20;
		if (s < 0) {
			s = 0;
		}
		int e = offset + length + 20;
		if (e > query.length()) {
			e = query.length();
		}
		String start = query.substring(s, offset);
		String end = query.substring(offset + length, e);
		String problematicPart = query.substring(offset, offset + length);
		return start + "‹" + problematicPart + "›" + end;
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
