package de.uni_koblenz.jgralab.greql2.funlib.statistics;

import java.util.Collection;
import java.util.Map;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Count extends Function {

	public Count() {
		super("Computes the number of values in a collection.",
				Category.STATISTICS);
	}

	public Integer evaluate(Collection<Object> l) {
		return l.size();
	}

	public Integer evaluate(Map<?, ?> m) {
		return m.size();
	}
}
