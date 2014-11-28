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
package de.uni_koblenz.jgralab.greql.funlib.relations;

import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class LeThan extends Function {

	@Description(params = { "a", "b" }, description = "Determines if $a < b$. Alternative: a < b", categories = Category.RELATIONS)
	public LeThan() {
		super(2, 1, 0.5);
	}

	public Boolean evaluate(Number a, Number b) {
		if (a instanceof Double || b instanceof Double) {
			return Double.compare(a.doubleValue(), b.doubleValue()) < 0;
		}
		if (a instanceof Long || b instanceof Long) {
			return a.longValue() < b.longValue();
		}
		assert (a instanceof Integer && b instanceof Integer);
		return a.intValue() < b.intValue();
	}

	public <T extends Comparable<? super T>> Boolean evaluate(T a, T b) {
		return a.compareTo(b) < 0;
	}
}
