/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

/**
 * This is the normal recursive implementation of depth first search. For some
 * big graphs this algorithm won't work and create a
 * <code>StackOverflowError</code>. In this case better use the
 * <code>IterativeDepthFirstSearch</code>.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class RecursiveDepthFirstSearch extends DepthFirstSearch {

	public RecursiveDepthFirstSearch(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
	}

	public RecursiveDepthFirstSearch(Graph graph) {
		this(graph, null, null);
	}

	@Override
	public RecursiveDepthFirstSearch execute(Vertex root)
			throws AlgorithmTerminatedException {
		if (subgraph != null && !subgraph.get(root)
				|| visitedVertices.get(root)) {
			return this;
		}
		startRunning();

		if (level != null) {
			level.set(root, 0);
		}
		number.set(root, num);
		visitors.visitRoot(root);

		// do not handle the exception here
		dfs(root);

		done();
		return this;
	}

	private void dfs(Vertex currentVertex) throws AlgorithmTerminatedException {
		vertexOrder[num] = currentVertex;

		number.set(currentVertex, num);
		visitors.visitVertex(currentVertex);

		visitedVertices.set(currentVertex, true);
		num++;

		for (Edge currentEdge : currentVertex.incidences(traversalDirection)) {
			if (visitedEdges.get(currentEdge) || subgraph != null
					&& !subgraph.get(currentEdge) || navigable != null
					&& !navigable.get(currentEdge)) {
				continue;
			}
			Vertex nextVertex = currentEdge.getThat();
			assert (subgraph == null || subgraph.get(nextVertex));
			edgeOrder[eNum] = currentEdge;
			if(enumber != null){
				enumber.set(currentEdge, eNum);
			}
			visitors.visitEdge(currentEdge);
			visitedEdges.set(currentEdge, true);
			eNum++;
			if (!visitedVertices.get(nextVertex)) {
				if (level != null) {
					level.set(nextVertex, level.get(currentVertex) + 1);
				}
				if (parent != null) {
					parent.set(currentEdge.getThat(), currentEdge);
				}
				visitors.visitTreeEdge(currentEdge);

				cancelIfInterrupted();

				// recursive call
				dfs(nextVertex);

				visitors.leaveTreeEdge(currentEdge);
			} else {
				visitors.visitFrond(currentEdge);
				if (!rnumber.isDefined(nextVertex)) {
					visitors.visitBackwardArc(currentEdge);
				} else if (number.get(currentVertex) < number.get(nextVertex)) {
					visitors.visitForwardArc(currentEdge);
				} else {
					visitors.visitCrosslink(currentEdge);
				}
			}
		}
		rnumber.set(currentVertex, rNum);
		if (rorder != null) {
			rorder[rNum] = currentVertex;
		}
		visitors.leaveVertex(currentVertex);
		rNum++;
	}

	@Override
	public RecursiveDepthFirstSearch execute()
			throws AlgorithmTerminatedException {
		super.execute();
		return this;
	}

}
