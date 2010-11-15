/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
	public RecursiveDepthFirstSearch execute(Vertex root) {
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
	public RecursiveDepthFirstSearch execute(){
		super.execute();
		return this;
	}

}
