package de.uni_koblenz.jgralab.greql2.funlib.bitops;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class BitAnd extends Function {
	public BitAnd() {
		super("Calculates the bitwise and $a \\vedge b$.", Category.ARITHMETICS);
	}

	public Integer evaluate(Integer a, Integer b) {
		return a.intValue() & b.intValue();
	}

	public Long evaluate(Long a, Long b) {
		return a.longValue() & b.longValue();
	}

	public Long evaluate(Long a, Integer b) {
		return a.longValue() & b.longValue();
	}

	public Long evaluate(Integer a, Long b) {
		return a.longValue() & b.longValue();
	}
}
