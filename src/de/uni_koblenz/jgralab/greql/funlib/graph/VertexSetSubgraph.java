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

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.funlib.NeedsGraphArgument;

@NeedsGraphArgument
public class VertexSetSubgraph extends Function {

	@Description(params = { "graph", "vertexSet" }, description = "Returns the subgraph induced by the vertex set, i.e. the vertices in vertexSet together with all edges between vertices in vertexSet.", categories = Category.GRAPH)
	public VertexSetSubgraph() {
		super(7, 1, 1.0);
	}

	public SubGraphMarker evaluate(Graph graph, PCollection<Vertex> vertexSet) {
		SubGraphMarker subgraphMarker = new SubGraphMarker(graph);
		for (Vertex currentVertex : vertexSet) {
			subgraphMarker.mark(currentVertex);
		}
		// add all edges
		for (Vertex currentVertex : vertexSet) {
			Edge currentEdge = currentVertex
					.getFirstIncidence(EdgeDirection.OUT);
			while (currentEdge != null) {
				if (subgraphMarker.isMarked(currentEdge.getThat())) {
					subgraphMarker.mark(currentEdge);
				}
				currentEdge = currentEdge.getNextIncidence(EdgeDirection.OUT);
			}
		}

		return subgraphMarker;
	}

}
