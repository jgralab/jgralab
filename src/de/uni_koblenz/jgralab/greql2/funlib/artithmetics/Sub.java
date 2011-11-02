package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Sub extends Function {
	public Sub() {
		super("Calculates the negateion $-a$. Alternative usage: - a.", 4, 1,
				1.0, Category.ARITHMETICS);
	}

	public Number evaluate(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() - b.doubleValue();
		} else if (a instanceof Long || b instanceof Long) {
			return a.longValue() - b.longValue();
		} else {
			return a.intValue() - b.intValue();
		}
	}
}