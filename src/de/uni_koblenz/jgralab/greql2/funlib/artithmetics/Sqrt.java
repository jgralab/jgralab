package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Sqrt extends Function {
	public Sqrt() {
		super("Computes $\\sqrt a$.", Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.sqrt(a.doubleValue());
	}
}
