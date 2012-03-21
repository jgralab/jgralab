/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql2.funlib.collections;

import java.util.ArrayList;
import java.util.Arrays;

import org.pcollections.ArrayPVector;
import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.Description;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Tuple;

public class Sort extends Function {

	@Description(params ="l", description = "Sorts the given collection according to natural ordering.",
			categories = Category.COLLECTIONS_AND_MAPS)
	public Sort() {
		super();
	}

	public PVector<?> evaluate(Tuple t) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends Comparable<? super T>> PVector<T> evaluate(
			PCollection<T> l) {
		if (l.isEmpty()) {
			if (l instanceof ArrayPVector) {
				return (PVector<T>) l;
			} else {
				return JGraLab.vector();
			}
		} else {
			Object[] sorted = new Object[l.size()];
			l.toArray(sorted);
			Arrays.sort(sorted);
			PVector<T> result = JGraLab.vector();
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
