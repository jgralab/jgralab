package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Neg extends Function {
	public Neg() {
		super("Calculates the difference $a-b$. Alternative usage: a - b.",
				Category.ARITHMETICS);
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
