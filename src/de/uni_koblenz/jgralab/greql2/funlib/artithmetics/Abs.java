package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Abs extends Function {
	public Abs() {
		super("Calculates the absolute value $|a|$.", 4, 1, 1.0,
				Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		if (a instanceof Double) {
			return Math.abs(a.doubleValue());
		} else if (a instanceof Long) {
			return Math.abs(a.longValue());
		} else {
			return Math.abs(a.intValue());
		}
	}
}
