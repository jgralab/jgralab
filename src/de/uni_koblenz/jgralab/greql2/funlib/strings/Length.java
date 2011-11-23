package de.uni_koblenz.jgralab.greql2.funlib.strings;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Length extends Function {
	public Length() {
		super("Returns the length of the given String.", Category.STRINGS);
	}

	public Integer evaluate(String s) {
		return s.length();
	}
}
