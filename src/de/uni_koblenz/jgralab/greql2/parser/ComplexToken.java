package de.uni_koblenz.jgralab.greql2.parser;

public class ComplexToken extends Token {
	
	public String value = null;
	
	public ComplexToken(TokenTypes type, int offset, int length, String value) {
		super(type, offset ,length);
		this.value=value;
	}

	public boolean isComplex() {
		return true;
	}
	
	public String getValue() {
		return value;
	}
	
}
