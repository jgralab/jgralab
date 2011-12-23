package de.uni_koblenz.jgralab.greql2.funlib.strings;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class EndsWith extends Function {
	public EndsWith() {
		super("Returns true, iff the given string ends with the given suffix.",
				3, 1, 0.05, Category.STRINGS);
	}

	public Boolean evaluate(String suffix, String s) {
		return s.endsWith(suffix);
	}
}
