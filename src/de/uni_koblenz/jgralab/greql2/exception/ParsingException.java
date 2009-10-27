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

	private static String surrounding(String query, int off, int len) {
		if ((len < 0) || (off < 0)) {
			return "";
		}
		int s = off - 20;
		if (s < 0) {
			s = 0;
		}
		int e = off + len + 20;
		if (e > query.length()) {
			e = query.length();
		}
		// System.out.println(off + ", " + len + ", " + s + ", " + e);
		String start = query.substring(s, off);
		String end = query.substring(off + len, e);
		String problematicPart = query.substring(off, off + len);
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
