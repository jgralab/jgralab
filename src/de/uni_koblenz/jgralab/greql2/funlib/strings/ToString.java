package de.uni_koblenz.jgralab.greql2.funlib.strings;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class ToString extends Function {
	public ToString() {
		super("Returns the string representation of the given object.",
				Category.STRINGS);
	}

	public String evaluate(Object o) {
		return o.toString();
	}
}
