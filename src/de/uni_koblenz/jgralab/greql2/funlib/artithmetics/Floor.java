package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Floor extends Function {
	public Floor() {
		super("Rounds $a$ by calculating $floor(a+0.5)$.", 4, 1, 1.0,
				Category.ARITHMETICS);
	}

	public Number evaluate(Number a) {
		return Math.floor(a.doubleValue());
	}
}
