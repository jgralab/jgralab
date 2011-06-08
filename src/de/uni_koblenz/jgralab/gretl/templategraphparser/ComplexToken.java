package de.uni_koblenz.jgralab.gretl.templategraphparser;

public class ComplexToken extends Token {
	String value;

	public ComplexToken(TokenType type, int offset, int length, String value) {
		super(type, offset, length);
		if ((type != TokenType.IDENT) && (type != TokenType.STRING)) {

		}
		this.value = value;
	}

	@Override
	public boolean isComplex() {
		return true;
	}

	@Override
	public String toString() {
		return type.toString() + "(" + value + ")";
	}
}
