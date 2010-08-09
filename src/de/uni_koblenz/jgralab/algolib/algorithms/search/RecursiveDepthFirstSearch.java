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
		if (visitedVertices.get(root) || !subgraph.get(root)) {
			return this;
		}
		startRunning();
		visitors.visitRoot(root);
		try {
			dfs(root);
		} catch (AlgorithmTerminatedException e) {
			
		}
		done();
		return this;
	}

	private void dfs(Vertex currentVertex) throws AlgorithmTerminatedException {
		vertexOrder[num] = currentVertex;
		visitors.visitVertex(currentVertex);
		visitedVertices.set(currentVertex, true);
		num++;

		for (Edge currentEdge : currentVertex.incidences(searchDirection)) {
			if (subgraph.get(currentEdge) && navigable.get(currentEdge)
					&& !visitedEdges.get(currentEdge)) {
				Vertex nextVertex = currentEdge.getThat();
				if (subgraph.get(nextVertex)) {
					edgeOrder[eNum] = currentEdge;
					visitors.visitEdge(currentEdge);
					visitedEdges.set(currentEdge, true);
					eNum++;
					if (!visitedVertices.get(nextVertex)) {
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
		visitors.leaveVertex(currentVertex);
		rNum++;
	}

}
