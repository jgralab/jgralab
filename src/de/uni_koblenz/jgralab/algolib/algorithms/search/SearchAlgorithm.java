package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.HybridGraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.ArrayFunction;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntDomainFunction;
import de.uni_koblenz.jgralab.algolib.problems.TraversalFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.graphmarker.BitSetEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetVertexMarker;

public abstract class SearchAlgorithm extends HybridGraphAlgorithm implements
		TraversalFromVertexSolver {

	public static final BooleanFunction<Edge> DEFAULT_NAVIGABLE = new BooleanFunction<Edge>() {

		@Override
		public boolean get(Edge parameter) {
			return true;
		}

		@Override
		public boolean isDefined(Edge parameter) {
			return true;
		}

		@Override
		public void set(Edge parameter, boolean value) {
			throw new UnsupportedOperationException(
					"This function is immutable.");
		}

	};
	public static final EdgeDirection DEFAULT_SEARCH_DIRECTION = EdgeDirection.OUT;

	protected BooleanFunction<Edge> navigable;
	protected EdgeDirection searchDirection;

	protected Vertex[] vertexOrder;
	protected Edge[] edgeOrder;
	protected BooleanFunction<Vertex> visitedVertices;
	protected BooleanFunction<Edge> visitedEdges;
	protected int num;
	protected int eNum;

	public SearchAlgorithm(Graph graph, BooleanFunction<GraphElement> subgraph,
			boolean directed, BooleanFunction<Edge> navigable) {
		super(graph, subgraph, directed);
		this.navigable = navigable;
	}

	public SearchAlgorithm(Graph graph) {
		super(graph);
	}

	@Override
	public void reset() {
		vertexOrder = new Vertex[graph.getVCount() + 1];
		edgeOrder = new Edge[graph.getECount() + 1];
		visitedVertices = new BitSetVertexMarker(graph);
		visitedEdges = new BitSetEdgeMarker(graph);

		num = 1;
		eNum = 1;
		// reset visitors (in subclass)
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		this.navigable = DEFAULT_NAVIGABLE;
		this.searchDirection = DEFAULT_SEARCH_DIRECTION;
	}

	public abstract void addSearchVisitor(SearchVisitor visitor);

	public BooleanFunction<Vertex> getVisitedVertices() {
		return visitedVertices;
	}

	public BooleanFunction<Edge> getVisitedEdges() {
		return visitedEdges;
	}

	public Edge[] getIntermediateEdgeOrder() {
		return edgeOrder;
	}

	public Vertex[] getIntermediateVertexOrder() {
		return vertexOrder;
	}

	public int getIntermediateNum() {
		return num;
	}

	public int getIntermediateENum() {
		return eNum;
	}

	@Override
	public IntDomainFunction<Edge> getEdgeOrder() {
		if (state == AlgorithmStates.FINISHED
				|| state == AlgorithmStates.STOPPED) {
			return new ArrayFunction<Edge>(edgeOrder);
		} else {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ state);
		}
	}

	@Override
	public IntDomainFunction<Vertex> getVertexOrder() {
		if (state == AlgorithmStates.FINISHED
				|| state == AlgorithmStates.STOPPED) {
			return new ArrayFunction<Vertex>(vertexOrder);
		} else {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ state);
		}
	}

	/**
	 * Sets the search direction to "OUT" if <code>directed == true</code> and
	 * to "INOUT" otherwise. For searching backwards, use
	 * <code>setSearchDirection</code>.
	 */
	@Override
	public void setDirected(boolean directed) {
		if (getState() == AlgorithmStates.INITIALIZED) {
			searchDirection = directed ? EdgeDirection.OUT
					: EdgeDirection.INOUT;
		} else {
			throw new IllegalStateException(
					"Parameters may only be changed when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

	/**
	 * Only returns <code>false</code> if <code>edgeDirection</code> is set to
	 * "INOUT".
	 */
	@Override
	public boolean isDirected() {
		return searchDirection != EdgeDirection.INOUT;
	}

	public void setSearchDirection(EdgeDirection searchDirection) {
		if (getState() == AlgorithmStates.INITIALIZED) {
			this.searchDirection = searchDirection;
		} else {
			throw new IllegalStateException(
					"Parameters may only be changed when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

	public EdgeDirection getSearchDirection() {
		return searchDirection;
	}

}
