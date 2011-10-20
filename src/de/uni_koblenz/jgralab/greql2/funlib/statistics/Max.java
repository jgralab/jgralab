package de.uni_koblenz.jgralab.greql2.funlib.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Max extends Function {

	public Max() {
		super(
				"Computes the maximum of a collection of values, or of two numbers $a$ and $b$.",
				Category.STATISTICS);
	}

	public Number evaluate(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return Math.max(a.doubleValue(), b.doubleValue());
		} else if (a instanceof Long || b instanceof Long) {
			return Math.max(a.longValue(), b.longValue());
		} else {
			return Math.max(a.intValue(), b.intValue());
		}
	}

	public <T extends Comparable<T>> T evaluate(Collection<T> l) {
		if (l.isEmpty()) {
			return null;
		} else {
			Iterator<T> it = l.iterator();
			T max = it.next();
			while (it.hasNext()) {
				T current = it.next();
				if (current.compareTo(max) > 0) {
					max = current;
				}
			}
			return max;
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}
}
