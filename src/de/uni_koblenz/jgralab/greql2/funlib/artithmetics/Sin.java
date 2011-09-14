package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Sin extends Function {
	public Sin() {
		super("Computes $\\sin a$.", Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.sin(a.doubleValue());
	}
}
