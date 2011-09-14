package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Tan extends Function {
	public Tan() {
		super("Computes $\\tan a$.", Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.tan(a.doubleValue());
	}
}
