package de.uni_koblenz.jgralab.greql2.funlib.statistics;

import java.util.Collection;
import java.util.Map;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Count extends Function {

	public Count() {
		super("Returns the number of items in the given collection or map.",
				Category.STATISTICS, Category.COLLECTIONS_AND_MAPS);
	}

	public Integer evaluate(Collection<Object> l) {
		return l.size();
	}

	public Integer evaluate(Map<?, ?> m) {
		return m.size();
	}
}
