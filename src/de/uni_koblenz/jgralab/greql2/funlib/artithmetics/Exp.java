package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Exp extends Function {
	public Exp() {
		super(
				"Returns Euler's number e raised to the power of the given number.",
				4, 1, 1.0, Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.exp(a.doubleValue());
	}
}
