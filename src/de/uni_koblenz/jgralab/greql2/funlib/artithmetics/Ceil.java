package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Ceil extends Function {
	public Ceil() {
		super("Returns the ceiling of the given number.", 4, 1, 1.0,
				Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.ceil(a.doubleValue());
	}
}
