package de.uni_koblenz.jgralab.greql2.funlib.logics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class And extends Function {
	public And() {
		super("Logical AND. Can be used as infix operator: a and b.", 2,
				1, 0.5, Category.LOGICS);
	}

	public Boolean evaluate(Boolean a, Boolean b) {
		return a && b;
	}
}
