package de.uni_koblenz.jgralab.greql2.funlib.logics;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Or extends Function {
	public Or() {
		super("Logical operation $a\\vee b$.\n" + "Alternative usage: a or b.",
				1, 1, 0.5, Category.LOGICS);
	}

	public Boolean evaluate(Boolean a, Boolean b) {
		return a || b;
	}
}
