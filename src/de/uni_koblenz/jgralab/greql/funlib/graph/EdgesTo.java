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

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;

@Deprecated
// use inIncidences instead
public class EdgesTo extends Function {

	public EdgesTo() {
		super(2, 5, 1.0);
	}

	@Description(params = "v", description = "(deprecated, use inIncidences) Returns the list of incoming edges of the given vertex.", categories = Category.GRAPH)
	public PVector<Edge> evaluate(Vertex v) {
		return evaluate(v, null);
	}

	@Description(params = { "v", "tc" }, description = "(deprecated, use inIncidences) Returns the list of incoming edges of the given vertex restricted by a type collection.", categories = Category.GRAPH)
	public PVector<Edge> evaluate(Vertex v, TypeCollection tc) {
		PVector<Edge> result = JGraLab.vector();
		for (Edge e : v.incidences(EdgeDirection.IN)) {
			if ((tc == null) || tc.acceptsType(e.getAttributedElementClass())) {
				result = result.plus(e);
			}
		}
		return result;
	}
}
