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
package de.uni_koblenz.jgralab.greql.funlib.collections;

import java.util.ArrayList;

import org.pcollections.PMap;
import org.pcollections.POrderedSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.types.Table;
import de.uni_koblenz.jgralab.greql.types.Tuple;

public class Get extends Function {

	public Get() {
		super(2, 1, 1.0);
	}

	@Description(params = { "v", "i" }, description = "Returns the value stored in v at index i. Short notation: v[i]", categories = Category.COLLECTIONS_AND_MAPS)
	public <T> T evaluate(PVector<T> v, Integer i) {
		return (i < 0) || (i >= v.size()) ? null : v.get(i);
	}

	@Description(params = { "s", "i" }, description = "Returns the value stored in s at index i. Short notation: s[i].", categories = Category.COLLECTIONS_AND_MAPS)
	public <T> T evaluate(POrderedSet<T> s, Integer i) {
		return (i < 0) || (i >= s.size()) ? null : s.get(i);
	}

	@Description(params = { "t", "i" }, description = "Returns the value stored in t at index i. Short notation: t[i]", categories = Category.COLLECTIONS_AND_MAPS)
	public <T> T evaluate(Table<T> l, Integer i) {
		return (i < 0) || (i >= l.size()) ? null : l.get(i);
	}

	@Description(params = { "t", "i" }, description = "Returns the i-th of tuple t. Short notation: t[i]", categories = Category.COLLECTIONS_AND_MAPS)
	public Object evaluate(Tuple t, Integer i) {
		return (i < 0) || (i >= t.size()) ? null : t.get(i);
	}

	@Description(params = { "map", "key" }, description = "Returns the map value associated with key. Short notation: map[key]", categories = Category.COLLECTIONS_AND_MAPS)
	public <K, V> V evaluate(PMap<K, V> m, K key) {
		return m.get(key);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0);
	}

	@Override
	public long getEstimatedCardinality(long inElements) {
		return inElements;
	}
}
