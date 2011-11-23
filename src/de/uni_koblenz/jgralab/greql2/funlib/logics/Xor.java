package de.uni_koblenz.jgralab.greql2.funlib.logics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Xor extends Function {
	public Xor() {
		super(
				"Logical XOR, i.e., $(a \\wedge \\neg b) \\vee (\\neg a\\wedge b)$.",
				2, 1, 1.0 / 3, Category.LOGICS);
	}

	public Boolean evaluate(Boolean a, Boolean b) {
		return a ^ b;
	}
}
