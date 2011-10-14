package de.uni_koblenz.jgralab.greql2.funlib.statistics;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Variance extends Function {

	public Variance() {
		super(
				"Computes the variance of a collection of numbers.\n"
						+ "If the size of the collection is less than 2, the variance is undefined.",
				Category.STATISTICS);
	}

	public Number evaluate(Collection<Number> l) {
		if (l.size() < 1) {
			return null;
		} else {
			double sum = 0.0;
			for (Number n : l) {
				sum += n.doubleValue();
			}
			double mean = sum / l.size();
			sum = 0.0;
			for (Number n : l) {
				double d = n.doubleValue() - mean;
				sum += d * d;
			}
			return sum / (l.size() - 1);
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}
}
