package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

public class RecursiveDepthFirstSearch extends DepthFirstSearch {

	public RecursiveDepthFirstSearch(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, directed, navigable);
	}

	public RecursiveDepthFirstSearch(Graph graph) {
		super(graph);
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

		for (Edge currentEdge : currentVertex.incidences(searchDirection)) {
			if (subgraph == null || (subgraph.get(currentEdge))
					&& (navigable == null || navigable.get(currentEdge))
					&& !visitedEdges.get(currentEdge)) {
				Vertex nextVertex = currentEdge.getThat();
				if (subgraph == null || subgraph.get(nextVertex)) {
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

						// recursive call
						dfs(nextVertex);

						visitors.leaveTreeEdge(currentEdge);
					} else {
						visitors.visitFrond(currentEdge);
						if (!rnumber.isDefined(nextVertex)) {
							visitors.visitBackwardArc(currentEdge);
						} else if (number.get(nextVertex) > number
								.get(currentVertex)) {
							visitors.visitForwardArc(currentEdge);
						} else {
							visitors.visitCrosslink(currentEdge);
						}
					}
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

}
