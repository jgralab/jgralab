package de.uni_koblenz.jgralab.gretl.templategraphparser;

public class Token {
	TokenType type;
	int offset;
	int length;

	public Token(TokenType type, int offset, int length) {
		this.type = type;
		this.offset = offset;
		this.length = length;
	}

	public boolean isComplex() {
		return false;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
