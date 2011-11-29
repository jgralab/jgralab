package de.uni_koblenz.jgralab.greql2.funlib.relations;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Nequals extends Function {

	public Nequals() {
		super("Determines if $a$ and $b$ are different. Alternative: a <> b", 1, 1,
				0.95, Category.RELATIONS);
	}

	public Boolean evaluate(String s, Enum<?> e) {
		return !s.equals(e.toString());
	}

	public Boolean evaluate(Enum<?> e, String s) {
		return !s.equals(e.toString());
	}

	public Boolean evaluate(Number a, Number b) {
		if ((a instanceof Double) || (b instanceof Double)) {
			return a.doubleValue() != b.doubleValue();
		} else if ((a instanceof Long) || (b instanceof Long)) {
			return a.longValue() != b.longValue();
		} else {
			return a.intValue() != b.intValue();
		}
	}

	public Boolean evaluate(Object a, Object b) {
		return !a.equals(b);
	}
}
