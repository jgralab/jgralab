package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class ToDouble extends Function {
	public ToDouble() {
		super("Converts a Number into a Double.", 1, 1, 1.0,
				Category.ARITHMETICS);
	}

	public Double evaluate(Number a) {
		return a.doubleValue();
	}
}
