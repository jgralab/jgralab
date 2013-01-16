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
package de.uni_koblenz.jgralab.greql.funlib.statistics;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class Sum extends Function {

	@Description(params = "l", description = "Returns the sum of the given collection of numbers.",
			categories = Category.STATISTICS)
	public Sum() {
		super();
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
