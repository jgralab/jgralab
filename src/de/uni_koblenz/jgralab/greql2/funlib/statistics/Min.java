package de.uni_koblenz.jgralab.greql2.funlib.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Min extends Function {

	public Min() {
		super(
				"Returns the minimum of a collection of comparable things or the minimum of the given two numbers.",
				Category.STATISTICS);
	}

	public Number evaluate(Number a, Number b) {
		if ((a instanceof Double) || (b instanceof Double)) {
			return Math.min(a.doubleValue(), b.doubleValue());
		} else if ((a instanceof Long) || (b instanceof Long)) {
			return Math.min(a.longValue(), b.longValue());
		} else {
			return Math.min(a.intValue(), b.intValue());
		}
	}

	public <T extends Comparable<T>> T evaluate(Collection<T> l) {
		if (l.isEmpty()) {
			return null;
		} else {
			Iterator<T> it = l.iterator();
			T min = it.next();
			while (it.hasNext()) {
				T current = it.next();
				if (current.compareTo(min) < 0) {
					min = current;
				}
			}
			return min;
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}
}
