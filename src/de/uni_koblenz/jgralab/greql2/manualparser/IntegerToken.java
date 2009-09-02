package de.uni_koblenz.jgralab.greql2.manualparser;

public class IntegerToken extends Token {
	
	Integer value = null;
	
	Integer decValue = null;
	
	public IntegerToken(TokenTypes type, int offset, int length, Integer value, Integer decValue) {
		super(type, offset ,length);
		this.value=value;
		this.decValue = decValue;
	}

	public boolean isComplex() {
		return true;
	}
	
	public String getValue() {
		return value.toString();
	}
	
	public Integer getNumber() {
		return value;
	}
	
	
	public Integer getDecValue() {
		return decValue;
	}
	
}
