package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.TraversalFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.visitors.SearchVisitorComposition;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class BreadthFirstSearch extends SearchAlgorithm implements
		TraversalFromVertexSolver {

	private SearchVisitorComposition visitors;

	public BreadthFirstSearch(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, directed, navigable);
	}

	public BreadthFirstSearch(Graph graph) {
		super(graph);
	}

	protected int firstV;
	
	protected int getIntermediateFirstV(){
		return firstV;
	}

	@Override
	public void reset() {
		super.reset();
		firstV = 2;
		visitors.reset();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new SearchVisitorComposition();
	}

	@Override
	public void addVisitor(Visitor visitor) {
		visitor.setAlgorithm(this);
		visitors.addVisitor(visitor);
	}

	@Override
	public BreadthFirstSearch execute(Vertex root) {
		if (visitedVertices.get(root) || !subgraph.get(root)) {
			return this;
		}
		startRunning();
		firstV--; // to make it work if the algorithm is resumed
		vertexOrder[num] = root;
		visitors.visitRoot(root);
		visitors.visitVertex(root);
		visitedVertices.set(root, true);
		num++;
		// main loop
		while (firstV < num && vertexOrder[firstV] != null) {
			Vertex currentVertex = vertexOrder[firstV++]; // pop
			for (Edge currentEdge : currentVertex.incidences(searchDirection)) {
				if (subgraph.get(currentEdge) && navigable.get(currentEdge)
						&& !visitedEdges.get(currentEdge)) {
					Vertex nextVertex = currentEdge.getThat();
					// TODO is this check necessary?
					if (subgraph.get(nextVertex)) {
						edgeOrder[eNum] = currentEdge;
						visitors.visitEdge(currentEdge);
						visitedEdges.set(currentEdge, true);
						eNum++;

						if (visitedVertices.get(nextVertex)) {
							visitors.visitFrond(currentEdge);
						} else {
							vertexOrder[num] = nextVertex;
							visitors.visitTreeEdge(currentEdge);
							visitors.visitVertex(nextVertex);
							visitedVertices.set(nextVertex, true);
							num++;
						}
					}
				}
			}
		}
		done();
		return this;
	}
}
