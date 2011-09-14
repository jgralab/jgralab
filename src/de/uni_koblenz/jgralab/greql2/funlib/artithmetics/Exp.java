package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Exp extends Function {
	public Exp() {
		super("Computes $e^a$.", Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.exp(a.doubleValue());
	}
}
