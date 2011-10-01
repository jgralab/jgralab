package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Ln extends Function {
	public Ln() {
		super("Computes $\\ln a$.", 4, 1, 1.0, Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.log(a.doubleValue());
	}
}
