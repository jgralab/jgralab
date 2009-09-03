package de.uni_koblenz.jgralab.greql2.parser;

public abstract class Token {

	public TokenTypes type;
	
	private int offset;
	
	private int length;
	
	public Token(TokenTypes type, int offset, int length) {
		this.type = type;
		this.offset = offset;
		this.length = offset;
	}
	
	public boolean isComplex() {
		return false;
	}
	
	public String getValue() {
		return ManualGreqlLexer.getTokenString(type);
	}

	
	public int getOffset() {
		return offset;
	}
	
	public int getLength() {
		return length;
	}
	
}
