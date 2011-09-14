package de.uni_koblenz.jgralab.greql2.funlib.strings;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class CapitalizeFirst extends Function {
	public CapitalizeFirst() {
		super(
				"Returns the given string with the first character made uppercase.",
				Category.STRINGS);
	}

	public String evaluate(String s) {
		if (s.length() == 0) {
			return s;
		}
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
