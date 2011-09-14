package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Mod extends Function {
	public Mod() {
		super(
				"Calculates the remainder of the division $a/b$. Alternative usage: a % b.",
				Category.ARITHMETICS);
	}

	public Number evaluate(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return a.doubleValue() % b.doubleValue();
		} else if (a instanceof Long || b instanceof Long) {
			return a.longValue() % b.longValue();
		} else {
			return a.intValue() % b.intValue();
		}
	}
}
