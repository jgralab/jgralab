/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql.funlib.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class Max extends Function {

	public Max() {
		super();
	}

	@Description(params = {"a","b"}, description = "Returns the maximum of the given two numbers.",
			categories = Category.STATISTICS)
	public Number evaluate(Number a, Number b) {
		if ((a instanceof Double) || (b instanceof Double)) {
			return Math.max(a.doubleValue(), b.doubleValue());
		} else if ((a instanceof Long) || (b instanceof Long)) {
			return Math.max(a.longValue(), b.longValue());
		} else {
			return Math.max(a.intValue(), b.intValue());
		}
	}

	@Description(params = {"l"}, description = "Returns the maximum of a collection of comparable things.",
			categories = {Category.STATISTICS, Category.COLLECTIONS_AND_MAPS})
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
