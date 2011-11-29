package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Floor extends Function {
	public Floor() {
		super("Returns the floor of the given number.", 4, 1, 1.0,
				Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.floor(a.doubleValue());
	}
}
