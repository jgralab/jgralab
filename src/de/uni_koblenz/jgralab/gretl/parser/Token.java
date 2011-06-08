package de.uni_koblenz.jgralab.gretl.parser;

public class Token {
	public final TokenTypes type;
	public final String value;
	public final int start;
	public final int end;

	Token(TokenTypes t, String v, int s, int e) {
		type = t;
		value = v;
		start = s;
		end = e;
		if (value.isEmpty() && (type != TokenTypes.EOF)) {
			throw new RuntimeException("Invalid Token " + this);
		}
		// System.out.println("Matched: " + this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("'");
		sb.append(value);
		sb.append("'");
		sb.append(" (");
		sb.append(type);
		sb.append(", [");
		sb.append(start);
		sb.append(", ");
		sb.append(end);
		sb.append("])");
		return sb.toString();
	}

}