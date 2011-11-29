package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Neg extends Function {
	public Neg() {
		super("Negates the given number. Can be used as unary operator: -x.",
				4, 1, 1.0, Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		if (a instanceof Double) {
			return -a.doubleValue();
		} else if (a instanceof Long) {
			return -a.longValue();
		} else {
			return -a.intValue();
		}
	}
}
