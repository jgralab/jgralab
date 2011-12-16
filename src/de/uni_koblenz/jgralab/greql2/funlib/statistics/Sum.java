package de.uni_koblenz.jgralab.greql2.funlib.statistics;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Sum extends Function {

	public Sum() {
		super("Returns the sum of the given collection of numbers.",
				Category.STATISTICS);
	}

	public Number evaluate(Collection<Number> l) {
		if (l.isEmpty()) {
			return 0;
		} else {
			Class<? extends Number> resultType = Integer.class;
			// determine best fitting result type
			for (Number n : l) {
				if (n instanceof Integer) {
					continue;
				}
				if (n instanceof Long) {
					if (resultType == Integer.class) {
						resultType = Long.class;
					}
					continue;
				}
				if (n instanceof Double) {
					resultType = Double.class;
					break;
				}
				throw new IllegalArgumentException(
						"sum can't handle numbers of type " + n.getClass());
			}
			if (resultType == Integer.class) {
				int sum = 0;
				for (Number n : l) {
					sum += n.intValue();
				}
				return sum;
			}
			if (resultType == Long.class) {
				long sum = 0;
				for (Number n : l) {
					sum += n.longValue();
				}
				return sum;
			}
			double sum = 0;
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
