package de.uni_koblenz.jgralab.greql2.funlib.bitops;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class BitXor extends Function {
	public BitXor() {
		super("Calculates the bitwise exlusive or $a xor b$.",
				Category.ARITHMETICS);
	}

	public Integer evaluate(Integer a, Integer b) {
		return a.intValue() ^ b.intValue();
	}

	public Long evaluate(Long a, Long b) {
		return a.longValue() ^ b.longValue();
	}

	public Long evaluate(Long a, Integer b) {
		return a.longValue() ^ b.longValue();
	}

	public Long evaluate(Integer a, Long b) {
		return a.longValue() ^ b.longValue();
	}
}
