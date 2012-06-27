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
package de.uni_koblenz.jgralab.greql.funlib.graph;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.types.Path;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;

public class InDegree extends DegreeFunction {

	public InDegree() {
		super(EdgeDirection.IN);
	}

	@Description(params = "v", description = "Returns the in-degree of the given vertex.",
			categories = Category.GRAPH)
	@Override
	public Integer evaluate(Vertex v) {
		return super.evaluate(v);
	}

	@Description(params = {"v","c"},description = "Returns the in-degree of the given vertex.\n"
			+ "The scope is limited by an type collection.",
			categories = Category.GRAPH)
	@Override
	public Integer evaluate(Vertex v, TypeCollection c) {
		return super.evaluate(v, c);
	}
	
	@Description(params={"v","p"}, description = "Returns the in-degree of the given vertex.\n"
			+ "The scope is limited by a path, a path system.",
			categories = {Category.GRAPH, Category.PATHS_AND_PATHSYSTEMS_AND_SLICES})
	@Override
	public Integer evaluate(Vertex v, Path p) {
		return super.evaluate(v, p);
	}
}
