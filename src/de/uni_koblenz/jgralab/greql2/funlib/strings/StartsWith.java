package de.uni_koblenz.jgralab.greql2.funlib.strings;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class StartsWith extends Function {
	public StartsWith() {
		super(
				"Returns true, iff the given string starts with the given prefix, optionally beginning search at the given offset.",
				3, 1, 0.05, Category.STRINGS);
	}

	public Boolean evaluate(String prefix, String s) {
		return s.startsWith(prefix);
	}

	public Boolean evaluate(String prefix, String s, int offset) {
		return s.startsWith(prefix, offset);
	}
}
