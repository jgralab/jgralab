package de.uni_koblenz.jgralab.greql2.funlib.artithmetics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class ToLong extends Function {
	public ToLong() {
		super("Converts the given number into a Long.", 1, 1, 1.0,
				Category.ARITHMETICS);
	}

	public Long evaluate(Number a) {
		return a.longValue();
	}
}
