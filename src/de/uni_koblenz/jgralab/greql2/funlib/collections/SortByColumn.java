package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Tuple;

public class SortByColumn extends Function {

	public SortByColumn() {
		super("Sorts a collection of tuples $l$ by column $col$.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public PVector<Tuple> evaluate(Integer col, PCollection<Tuple> l) {
		if (l.isEmpty()) {
			return JGraLab.vector();
		} else {
			Tuple[] sorted = new Tuple[l.size()];
			l.toArray(sorted);
			Arrays.sort(sorted, new TupleComparator(col));
			PVector<Tuple> result = JGraLab.vector();
			for (Tuple x : sorted) {
				result = result.plus(x);
			}
			return result;
		}
	}

	private static class TupleComparator implements Comparator<Tuple> {
		private int column;

		public TupleComparator(int n) {
			column = n;
		}

		@SuppressWarnings("unchecked")
		@Override
		public int compare(Tuple t0, Tuple t1) {
			@SuppressWarnings("rawtypes")
			Comparable c0 = (Comparable) t0.get(column);
			@SuppressWarnings("rawtypes")
			Comparable c1 = (Comparable) t1.get(column);
			return c0.compareTo(c1);
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		long n = inElements.get(0);
		return (long) (2 * n * Math.log(n));
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
