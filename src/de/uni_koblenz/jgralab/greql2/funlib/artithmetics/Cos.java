package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Cos extends Function {
	public Cos() {
		super("Computes $\\cos a$.", 4, 1, 1.0, Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.cos(a.doubleValue());
	}
}
