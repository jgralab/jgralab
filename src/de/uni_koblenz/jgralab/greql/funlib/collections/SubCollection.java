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
package de.uni_koblenz.jgralab.greql.funlib.collections;

import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class SubCollection extends Function {

	public SubCollection() {
		super();
	}

	@Description(params = {"coll","startIndex","endIndex"}, description = "Returns a sub PVector starting at the given start index (including),\n"
						+ "and ending at the given end index (excluding).",
				categories = Category.COLLECTIONS_AND_MAPS)
	public <T> PVector<T> evaluate(PVector<T> coll, Integer startIndex,
			Integer endIndex) {
		if (startIndex < 0 || endIndex > coll.size() || startIndex > endIndex) {
			return null;
		}
		return coll.subList(startIndex, endIndex);
	}

	@Description(params = {"coll","startIndex"}, description = "Returns a sub PVector starting at the given start index (including).",
		categories = Category.COLLECTIONS_AND_MAPS)
	public <T> PVector<T> evaluate(PVector<T> coll, Integer startIndex) {
		return evaluate(coll, startIndex, coll.size());
	}

	@Description(params = {"coll","startIndex","endIndex"}, description = "Returns a sub PSet starting at the given start index (including),\n"
		+ "and ending at the given end index (excluding).",
		categories = Category.COLLECTIONS_AND_MAPS)
	public <T> PSet<T> evaluate(PSet<T> coll, Integer startIndex,
			Integer endIndex) {
		if (startIndex < 0 || endIndex > coll.size() || startIndex > endIndex) {
			return null;
		}
		PSet<T> result = JGraLab.set();
		int idx = 0;
		for (T item : coll) {
			if (idx == endIndex) {
				break;
			}
			if ((idx >= startIndex) && (idx < endIndex)) {
				result = result.plus(item);
			}
			idx++;
		}
		return result;
	}

	@Description(params = {"coll","startIndex"}, description = "Returns a sub PSet starting at the given start index (including).",
		categories = Category.COLLECTIONS_AND_MAPS)
	public <T> PSet<T> evaluate(PSet<T> coll, Integer startIndex) {
		return evaluate(coll, startIndex, coll.size());
	}
}
