package de.uni_koblenz.jgralab.greql2.funlib.logics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Not extends Function {
	public Not() {
		super("Logical operation $\\not a$.\n" + "Alternative usage: not a.",
				1, 1, 1, Category.LOGICS);
	}

	public Boolean evaluate(Boolean a) {
		return !a;
	}
}
