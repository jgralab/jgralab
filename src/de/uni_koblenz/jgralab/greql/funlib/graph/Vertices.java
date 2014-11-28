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
package de.uni_koblenz.jgralab.greql.funlib.graph;

import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.types.Path;
import de.uni_koblenz.jgralab.greql.types.PathSystem;

public class Vertices extends Function {

	public Vertices() {
		super();
	}

	@Description(params = "p", description = "Returns the set of vertices in the given path system.", categories = Category.PATHS_AND_PATHSYSTEMS_AND_SLICES)
	public PSet<Vertex> evaluate(PathSystem p) {
		return p.getVertices();
	}

	@Description(params = "s", description = "Returns the set of vertices in the given slice.", categories = Category.PATHS_AND_PATHSYSTEMS_AND_SLICES)
	public PSet<Vertex> evaluate(SubGraphMarker slice) {
		PSet<Vertex> s = JGraLab.set();
		for (Vertex v : slice.getMarkedVertices()) {
			if (v == null) {
				System.out.println(v);
			}
			s = s.plus(v);
		}
		return s;
	}

	@Description(params = "p", description = "Returns the list of vertices in the Path p.", categories = Category.PATHS_AND_PATHSYSTEMS_AND_SLICES)
	public PVector<Vertex> evaluate(Path p) {
		return p.getVertexTrace();
	}

}
