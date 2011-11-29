package de.uni_koblenz.jgralab.greql2.funlib.logics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Or extends Function {
	public Or() {
		super("Logical OR. Can be used as infix operator: a or b.", 2, 1,
				5.0 / 9, Category.LOGICS);
	}

	public Boolean evaluate(Boolean a, Boolean b) {
		return a || b;
	}
}
