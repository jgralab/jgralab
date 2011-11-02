package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Ceil extends Function {
	public Ceil() {
		super("Calculates the ceil of $a$.", 4, 1, 1.0, Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.ceil(a.doubleValue());
	}
}