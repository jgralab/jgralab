package de.uni_koblenz.jgralab.greql2.funlib.logics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Not extends Function {
	public Not() {
		super("Logical NOT. Can be used as unary operator: not a.", 2, 1,
				1.0 / 3, Category.LOGICS);
	}

	public Boolean evaluate(Boolean a) {
		return !a;
	}
}
