package de.uni_koblenz.jgralab.greql2.funlib.logics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Xor extends Function {
	public Xor() {
		super(
				"Logical operation $(a \\wedge \\neg b) \\vee (\\neg a\\wedge b)$.\n"
						+ "Alternative usage: a or b.", 2, 1, 1.0 / 3,
				Category.LOGICS);
	}

	public Boolean evaluate(Boolean a, Boolean b) {
		return a ^ b;
	}
}
