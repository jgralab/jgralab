package de.uni_koblenz.jgralab.greql2.funlib.statistics;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Sum extends Function {

	public Sum() {
		super("Computes the sum of a collection of numbers.",
				Category.STATISTICS);
	}

	public Double evaluate(Collection<Number> l) {
		if (l.isEmpty()) {
			return 0.0;
		} else {
			double sum = 0.0;
			for (Number n : l) {
				sum += n.doubleValue();
			}
			return sum;
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}
}
