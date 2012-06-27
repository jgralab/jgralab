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
package de.uni_koblenz.jgralab.greql.funlib.relations;

import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class Nequals extends Function {

	@Description(params = {"a","b"}, description = "Determines if $a$ and $b$ are different. Alternative: a <> b",
			categories = Category.RELATIONS)
	public Nequals() {
		super(1, 1, 0.95);
	}

	public Boolean evaluate(String s, Enum<?> e) {
		return !s.equals(e.toString());
	}

	public Boolean evaluate(Enum<?> e, String s) {
		return !s.equals(e.toString());
	}

	public Boolean evaluate(Number a, Number b) {
		if ((a instanceof Double) || (b instanceof Double)) {
			return a.doubleValue() != b.doubleValue();
		} else if ((a instanceof Long) || (b instanceof Long)) {
			return a.longValue() != b.longValue();
		} else {
			return a.intValue() != b.intValue();
		}
	}

	public Boolean evaluate(Object a, Object b) {
		return !a.equals(b);
	}
}
