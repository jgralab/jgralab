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
