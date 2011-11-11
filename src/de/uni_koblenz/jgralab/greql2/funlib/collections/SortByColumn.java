package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;

public class SortByColumn extends Function {

	public SortByColumn() {
		super("Sorts a collection of tuples $l$ by columns.",
				Category.COLLECTIONS_AND_MAPS);
	}

	public Table<Tuple> evaluate(Integer column, Table<Tuple> t) {
		PVector<Integer> columns = JGraLab.vector();
		return evaluate(columns.plus(column), t);
	}

	public Table<Tuple> evaluate(PVector<Integer> columns, Table<Tuple> t) {
		Table<Tuple> result = Table.empty();
		return result.withTitles(t.getTitles()).plusAll(
				evaluate(columns, t.toPVector()));
	}

	public PVector<Tuple> evaluate(Integer column, PCollection<Tuple> l) {
		PVector<Integer> columns = JGraLab.vector();
		return evaluate(columns.plus(column), l);
	}

	public PVector<Tuple> evaluate(PVector<Integer> columns,
			PCollection<Tuple> l) {
		if (columns.isEmpty()) {
			throw new IllegalArgumentException(
					"Parameter columns must contain at least one column number.");
		}
		if (l.isEmpty()) {
			return JGraLab.vector();
		} else {
			Tuple[] sorted = new Tuple[l.size()];
			l.toArray(sorted);
			Arrays.sort(sorted, new TupleComparator(columns));
			PVector<Tuple> result = JGraLab.vector();
			for (Tuple x : sorted) {
				result = result.plus(x);
			}
			return result;
		}
	}

	private static class TupleComparator implements Comparator<Tuple> {

		PVector<Integer> cols;

		TupleComparator(PVector<Integer> columns) {
			cols = columns;
		}

		@SuppressWarnings("unchecked")
		@Override
		public int compare(Tuple t0, Tuple t1) {
			int l = cols.size();
			int result = 0;
			for (int i = 0; i < l && result == 0; ++i) {
				int c = cols.get(i);
				@SuppressWarnings("rawtypes")
				Comparable c0 = (Comparable) t0.get(c);
				@SuppressWarnings("rawtypes")
				Comparable c1 = (Comparable) t1.get(c);
				result = c0.compareTo(c1);
			}
			return result;
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
