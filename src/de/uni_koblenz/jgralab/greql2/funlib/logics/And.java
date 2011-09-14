package de.uni_koblenz.jgralab.greql2.funlib.logics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class And extends Function {
	public And() {
		super("Logical operation $a\\wedge b$.\n"
				+ "Alternative usage: a and b.", 1, 1, 0.5, Category.LOGICS);
	}

	public Boolean evaluate(Boolean a, Boolean b) {
		return a && b;
	}
}
