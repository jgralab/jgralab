package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class ToInteger extends Function {
	public ToInteger() {
		super("Converts the given number into an Integer.", 1, 1, 1.0,
				Category.ARITHMETICS);
	}

	public Integer evaluate(Number a) {
		return a.intValue();
	}
}
