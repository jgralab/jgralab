package de.uni_koblenz.jgralab.greql2.funlib.bitops;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class BitNot extends Function {
	public BitNot() {
		super("Calculates the bitwise negation of the given number.", 4, 1,
				1.0, Category.ARITHMETICS);
	}

	public Integer evaluate(Integer a) {
		return ~a.intValue();
	}

	public Long evaluate(Long a) {
		return ~a.longValue();
	}

}
