package de.uni_koblenz.jgralab.greql2.parser;

public class RealToken extends Token {

	Double value = null;
	
	public RealToken(TokenTypes type, int offset, int length, Double value) {
		super(type, offset ,length);
		this.value=value;
	}

	public boolean isComplex() {
		return true;
	}
	
	public String getValue() {
		return value.toString();
	}
	
	public Double getNumber() {
		return value;
	}
	
}
