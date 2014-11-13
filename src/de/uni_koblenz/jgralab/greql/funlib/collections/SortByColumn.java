/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.greql.funlib.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.types.Table;
import de.uni_koblenz.jgralab.greql.types.Tuple;

public class SortByColumn extends Function {

	public SortByColumn() {
		super();
	}

	@Description(params = {"column", "t"}, description = "Sorts a table of tuples by one column.",
			categories = Category.COLLECTIONS_AND_MAPS)
	public Table<Tuple> evaluate(Integer column, Table<Tuple> t) {
		return evaluate(JGraLab.<Integer> vector().plus(column), t);
	}


	@Description(params = {"columns", "t"}, description = "Sorts a table of tuples by many columns.",
			categories = Category.COLLECTIONS_AND_MAPS)
	public Table<Tuple> evaluate(PVector<Integer> columns, Table<Tuple> t) {
		Table<Tuple> result = Table.empty();
		return result.withTitles(t.getTitles()).plusAll(
				evaluate(columns, t.toPVector()));
	}


	@Description(params = {"column", "l"}, description = "Sorts a collection of tuples by one column.",
			categories = Category.COLLECTIONS_AND_MAPS)
	public PVector<Tuple> evaluate(Integer column, PCollection<Tuple> l) {
		return evaluate(JGraLab.<Integer> vector().plus(column), l);
	}


	@Description(params = {"columns", "l"}, description = "Sorts a collection of tuples by many columns.",
			categories = Category.COLLECTIONS_AND_MAPS)
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
			for (int i = 0; (i < l) && (result == 0); ++i) {
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
	public long getEstimatedCardinality(long inElements) {
		return inElements;
	}
}
