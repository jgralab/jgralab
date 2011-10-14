package de.uni_koblenz.jgralab.greql2.funlib.strings;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class StartsWith extends Function {
	public StartsWith() {
		super(
				"Returns true iff the $s$ starts with $prefix$ (beginning search at $toffset$).",
				3, 1, 0.05, Category.STRINGS);
	}

	public Boolean evaluate(String prefix, String s) {
		return s.startsWith(prefix);
	}

	public Boolean evaluate(String prefix, String s, int toffset) {
		return s.startsWith(prefix, toffset);
	}
}
