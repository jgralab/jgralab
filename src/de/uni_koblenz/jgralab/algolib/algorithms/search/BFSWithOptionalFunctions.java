package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.problems.TraversalFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.visitors.SearchVisitorComposition;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class BFSWithOptionalFunctions extends SearchAlgorithm implements
		TraversalFromVertexSolver {

	private SearchVisitorComposition visitors = new SearchVisitorComposition();

	public BFSWithOptionalFunctions(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, directed, navigable);
	}

	public BFSWithOptionalFunctions(Graph graph) {
		super(graph);
	}

	protected int firstV;
	protected IntFunction<Vertex> level;
	protected IntFunction<Vertex> number;
	protected Function<Vertex, Edge> parent;

	protected int getIntermediateFirstV() {
		return firstV;
	}

	public BFSWithOptionalFunctions withLevel() {
		level = new IntegerVertexMarker(graph);
		return this;
	}

	public BFSWithOptionalFunctions withNumber() {
		number = new IntegerVertexMarker(graph);
		return this;
	}

	public BFSWithOptionalFunctions withParent() {
		parent = new ArrayVertexMarker<Edge>(graph);
		return this;
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
	public void solveTraversalFromVertex(Vertex root) {
		if (visitedVertices.get(root) || !subgraph.get(root)) {
			return;
		}
		startRunning();
		firstV--; // to make it work if the algorithm is resumed
		vertexOrder[num] = root;

		if (level != null) {
			level.set(root, 0);
		}
		visitors.visitRoot(root);

		if (number != null) {
			number.set(root, num);
		}

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
							if (level != null) {
								level.set(nextVertex,
										level.get(currentVertex) + 1);
							}
							if (parent != null) {
								parent.set(currentEdge.getThat(), currentEdge);
							}
							visitors.visitTreeEdge(currentEdge);
							if (number != null) {
								number.set(root, num);
							}
							visitors.visitVertex(nextVertex);
							visitedVertices.set(nextVertex, true);
							num++;
						}
					}
				}
			}
		}
		done();
	}

	public IntFunction<Vertex> getLevel() {
		return level;
	}

	public void setLevel(IntFunction<Vertex> level) {
		this.level = level;
	}

	public IntFunction<Vertex> getNumber() {
		return number;
	}

	public void setNumber(IntFunction<Vertex> number) {
		this.number = number;
	}

	public Function<Vertex, Edge> getParent() {
		return parent;
	}

	public void setParent(Function<Vertex, Edge> parent) {
		this.parent = parent;
	}
}
