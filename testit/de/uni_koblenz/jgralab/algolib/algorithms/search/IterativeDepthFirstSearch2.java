/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.algolib.algorithms.search;

import java.util.Stack;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

public class IterativeDepthFirstSearch2 extends DepthFirstSearch {

	private Stack<Edge> treeEdges;

	public IterativeDepthFirstSearch2(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
	}

	public IterativeDepthFirstSearch2(Graph graph) {
		this(graph, null, true, null);
	}

	public void reset() {
		super.reset();
		treeEdges = new Stack<Edge>();
	}

	@Override
	public SearchAlgorithm execute(Vertex root)
			throws AlgorithmTerminatedException {
		if (subgraph != null && !subgraph.get(root)
				|| visitedVertices.get(root)) {
			return this;
		}
		startRunning();

		// handle root as root
		if (level != null) {
			level.set(root, 0);
		}
		number.set(root, num);
		visitors.visitRoot(root);

		handleVertex(root);

		for (Edge currentRootIncidence : root.incidences(traversalDirection)) {
			if (visitedEdges.get(currentRootIncidence) || subgraph != null
					&& !subgraph.get(currentRootIncidence) || navigable != null
					&& !navigable.get(currentRootIncidence)) {
				continue;
			}
			treeEdges.push(currentRootIncidence);
			while (!treeEdges.isEmpty()) {
				Edge nextTreeEdge = treeEdges.pop();

			}

		}

		done();
		return this;
	}

	private void handleEdge(Edge edge) throws AlgorithmTerminatedException {
		
	}

	private void handleVertex(Vertex vertex)
			throws AlgorithmTerminatedException {
		vertexOrder[num] = vertex;

		number.set(vertex, num);
		visitors.visitVertex(vertex);

		visitedVertices.set(vertex, true);
		num++;
	}

	@Override
	public IterativeDepthFirstSearch2 execute()
			throws AlgorithmTerminatedException {
		super.execute();
		return this;
	}

}
