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

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.types.Path;
import de.uni_koblenz.jgralab.greql.types.PathSystem;

public class Contains extends Function {

	public Contains() {
		super(2, 1, 0.2);
	}

	@Description(params = { "s", "sub" }, description = "Returns true, iff s contains sub.", categories = Category.STRINGS)
	public Boolean evaluate(String string, String containedString) {
		return string.contains(containedString);
	}

	@Description(params = { "c", "el" }, description = "Returns true, iff c contains  el.", categories = Category.COLLECTIONS_AND_MAPS)
	public <T> Boolean evaluate(PCollection<T> c, T el) {
		return c.contains(el);
	}

	@Description(params = { "p", "el" }, description = "Returns true, iff p contains  el.", categories = Category.PATHS_AND_PATHSYSTEMS_AND_SLICES)
	public <T> Boolean evaluate(Path p, GraphElement<?, ?> el) {
		return p.contains(el);
	}

	@Description(params = { "p", "el" }, description = "Returns true, iff p contains  el.", categories = Category.PATHS_AND_PATHSYSTEMS_AND_SLICES)
	public <T> Boolean evaluate(PathSystem p, GraphElement<?, ?> el) {
		return p.contains(el);
	}

}
