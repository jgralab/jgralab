package de.uni_koblenz.jgralab.greql2.funlib.bitops;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class BitShl extends Function {
	public BitShl() {
		super("Shifts $a$ by $n$ bits to the left.", Category.ARITHMETICS);
	}

	public Integer evaluate(Integer a, Integer n) {
		return a.intValue() << n.intValue();
	}

	public Long evaluate(Long a, Integer n) {
		return a.longValue() << n.intValue();
	}
}
