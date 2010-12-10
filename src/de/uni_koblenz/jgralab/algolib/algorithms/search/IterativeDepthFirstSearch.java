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

import java.util.Iterator;
import java.util.Stack;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;

/**
 * This is the iterative implementation of depth first search. It behaves the
 * same way as the <code>RecursiveDepthFirstSearch</code> without depending on
 * the call stack of the Java VM. This avoids the problem arising from the
 * possible <code>StackOverflowError</code> that can occur using the
 * <code>RecursiveDepthFirstSearch</code>. However, this implementation requires
 * to make the optional result <code>parent</code> mandatory. This and the
 * additional overhead for runtime variables makes this implementation using
 * more memory than its recursive variant.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class IterativeDepthFirstSearch extends DepthFirstSearch {

	private ArrayVertexMarker<Iterator<Edge>> remainingIncidences;
	private Stack<Vertex> incompleteVertices;

	public IterativeDepthFirstSearch(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
	}

	public IterativeDepthFirstSearch(Graph graph) {
		super(graph);
	}

	@Override
	public void reset() {
		super.reset();
		parent = new ArrayVertexMarker<Edge>(graph);
		remainingIncidences = new ArrayVertexMarker<Iterator<Edge>>(graph);
		incompleteVertices = new Stack<Vertex>();
	}

	@Override
	public void disableOptionalResults() {
		super.disableOptionalResults();
		level = null;
		rorder = null;
	}

	@Override
	public DepthFirstSearch withParent() {
		checkStateForSettingParameters();
		throw new UnsupportedOperationException(
				"The result \"parent\" is mandatory for iterative DFS and doesn't need to be explicitly activated.");
	}

	@Override
	public DepthFirstSearch withoutParent() {
		checkStateForSettingParameters();
		throw new UnsupportedOperationException(
				"The result \"parent\" is mandatory for iterative DFS and cannot be deactivated.");
	}

	@Override
	public IterativeDepthFirstSearch execute(Vertex root)
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

		// remainingIncidences.mark(root, root.incidences().iterator());
		incompleteVertices.push(root);

		while (!incompleteVertices.isEmpty()) {
			// get next vertex from stack and visit it if it is the first time
			// Vertex currentVertex = incompleteVertices.pop();
			Vertex currentVertex = incompleteVertices.peek();

			if (!visitedVertices.get(currentVertex)) {
				vertexOrder[num] = currentVertex;

				number.set(currentVertex, num);
				remainingIncidences.mark(currentVertex, currentVertex
						.incidences(traversalDirection).iterator());
				visitors.visitVertex(currentVertex);

				visitedVertices.set(currentVertex, true);
				num++;
			}

			Iterator<Edge> currentIncidences = remainingIncidences
					.getMark(currentVertex);
			if (currentIncidences.hasNext()) {
				cancelIfInterrupted();
				Edge currentEdge = currentIncidences.next();
				if (visitedEdges.get(currentEdge) || subgraph != null
						&& !subgraph.get(currentEdge) || navigable != null
						&& !navigable.get(currentEdge)) {
					// incompleteVertices.push(currentVertex);
					continue;
				}
				Vertex nextVertex = currentEdge.getThat();
				assert (subgraph == null || subgraph.get(nextVertex));
				// visit current edge
				edgeOrder[eNum] = currentEdge;
				if(enumber != null){
					enumber.set(currentEdge, eNum);
				}
				visitors.visitEdge(currentEdge);
				visitedEdges.set(currentEdge, true);
				eNum++;

				if (visitedVertices.get(nextVertex)) {
					visitors.visitFrond(currentEdge);
					if (!rnumber.isDefined(nextVertex)) {
						visitors.visitBackwardArc(currentEdge);
					} else if (number.get(nextVertex) > number
							.get(currentVertex)) {
						visitors.visitForwardArc(currentEdge);
					} else {
						visitors.visitCrosslink(currentEdge);
					}
					// incompleteVertices.push(currentVertex);
				} else {
					if (level != null) {
						level.set(nextVertex, level.get(currentVertex) + 1);
					}

					parent.set(currentEdge.getThat(), currentEdge);

					visitors.visitTreeEdge(currentEdge);
					// incompleteVertices.push(currentVertex);
					incompleteVertices.push(nextVertex);
				}
			} else {
				incompleteVertices.pop(); // remove vertex from stack
				rnumber.set(currentVertex, rNum);
				if (rorder != null) {
					rorder[rNum] = currentVertex;
				}
				visitors.leaveVertex(currentVertex);
				rNum++;
				remainingIncidences.removeMark(currentVertex);
				// leave tree edge leading to the current vertex
				if (currentVertex != root) {
					visitors.leaveTreeEdge(parent.get(currentVertex));
				}
			}
			// nothing may follow here!!!
		}

		done();
		return this;
	}

	@Override
	public IterativeDepthFirstSearch execute()
			throws AlgorithmTerminatedException {
		super.execute();
		return this;
	}
}
