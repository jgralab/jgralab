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

import java.util.ArrayList;

import org.pcollections.ArrayPMap;
import org.pcollections.ArrayPSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class Union extends Function {

	public Union() {
		super();
	}

	@Description(params = {"a","b"}, description = "Computes the union of the given two sets.",
				categories = Category.COLLECTIONS_AND_MAPS)
	public <T> PSet<T> evaluate(PSet<T> a, PSet<T> b) {
		if (b.isEmpty()) {
			if (a instanceof ArrayPSet) {
				return a;
			} else {
				return JGraLab.set();
			}
		} else {
			if (a instanceof ArrayPSet) {
				return ((ArrayPSet<T>) a).plusAll(b);
			} else {
				return JGraLab.<T> set().plusAll(a).plusAll(b);
			}
		}
	}

	@Description(params = {"a","b"}, description = "Computes the union of the given maps.\n"
				+ "In case of common keys in maps, the entries of the second one override the first one's entries.",
				categories = Category.COLLECTIONS_AND_MAPS)
	public <K, V> PMap<K, V> evaluate(PMap<K, V> a, PMap<K, V> b) {
		if (b.isEmpty()) {
			if (a instanceof ArrayPMap) {
				return a;
			} else {
				return JGraLab.map();
			}
		} else {
			if (a instanceof ArrayPMap) {
				return ((ArrayPMap<K, V>) a).plusAll(b);
			} else {
				return JGraLab.<K, V> map().plusAll(a).plusAll(b);
			}

		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0) + inElements.get(1);
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return inElements;
	}
}
