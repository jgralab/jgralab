package de.uni_koblenz.jgralab.greql2.funlib.bitops;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class BitNot extends Function {
	public BitNot() {
		super("Calculates the bitwise negation $\not a$.", Category.ARITHMETICS);
	}

	public Integer evaluate(Integer a) {
		return ~a.intValue();
	}

	public Long evaluate(Long a, Long b) {
		return ~a.longValue();
	}

}
