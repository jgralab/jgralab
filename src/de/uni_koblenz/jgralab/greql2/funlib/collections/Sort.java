package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;
import java.util.Arrays;

import org.pcollections.ArrayPVector;
import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Sort extends Function {

	public Sort() {
		super("Sorts a collection of values.", Category.COLLECTIONS_AND_MAPS);
	}

	@SuppressWarnings("unchecked")
	public <T extends Comparable<? super T>> PVector<T> evaluate(
			PCollection<T> l) {
		if (l.isEmpty()) {
			if (l instanceof ArrayPVector) {
				return (PVector<T>) l;
			} else {
				return ArrayPVector.empty();
			}
		} else {
			Object[] sorted = new Object[l.size()];
			l.toArray(sorted);
			Arrays.sort(sorted);
			PVector<T> result = ArrayPVector.empty();
			for (Object x : sorted) {
				result = result.plus((T) x);
			}
			return result;
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		long n = inElements.get(0);
		return (long) (n * Math.log(n));
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
