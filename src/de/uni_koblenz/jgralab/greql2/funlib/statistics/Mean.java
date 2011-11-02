package de.uni_koblenz.jgralab.greql2.funlib.statistics;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Mean extends Function {

	public Mean() {
		super("Computes the mean value of a collection of numbers.",
				Category.STATISTICS);
	}

	public Double evaluate(Collection<Number> l) {
		if (l.isEmpty()) {
			return null;
		} else {
			double sum = 0.0;
			for (Number n : l) {
				sum += n.doubleValue();
			}
			return sum / l.size();
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}
}
