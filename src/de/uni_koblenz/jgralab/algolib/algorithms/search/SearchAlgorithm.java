package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.ArrayPermutation;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.problems.CompleteTraversalSolver;
import de.uni_koblenz.jgralab.algolib.problems.TraversalFromVertexSolver;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public abstract class SearchAlgorithm extends GraphAlgorithm implements
		TraversalFromVertexSolver, CompleteTraversalSolver {

	/**
	 * This is the default value for the parameter <code>searchDirection</code>.
	 * By default the algorithm follows only outgoing edges, which also means
	 * that the graph is interpreted as a directed graph.
	 */
	public static final EdgeDirection DEFAULT_SEARCH_DIRECTION = EdgeDirection.OUT;

	/**
	 * A function that tells if a reachable edge is also navigable.
	 */
	protected BooleanFunction<Edge> navigable;

	/**
	 * The search direction this search algorithm uses.
	 */
	protected EdgeDirection searchDirection;

	/**
	 * The intermediate result <code>vertexOrder</code>.
	 */
	protected Vertex[] vertexOrder;

	/**
	 * The intermediate result <code>edgeOrder</code>.
	 */
	protected Edge[] edgeOrder;

	/**
	 * A marker for visited vertices.
	 */
	protected BooleanFunction<Vertex> visitedVertices;

	/**
	 * A marker for visited edges.
	 */
	protected BooleanFunction<Edge> visitedEdges;

	/**
	 * A runtime variable needed to compute <code>vertexOrder</code>.
	 */
	protected int num;

	/**
	 * A runtime variable needed to compute <code>edgeOrder</code>.
	 */
	protected int eNum;

	// optional functions

	protected IntFunction<Vertex> level;
	protected IntFunction<Vertex> number;
	protected Function<Vertex, Edge> parent;

	public SearchAlgorithm(Graph graph, BooleanFunction<GraphElement> subgraph,
			boolean directed, BooleanFunction<Edge> navigable) {
		super(graph, subgraph);
		setDirected(directed);
		this.navigable = navigable;
	}

	public SearchAlgorithm(Graph graph) {
		super(graph);
	}

	public SearchAlgorithm withLevel() {
		checkStateForSettingParameters();
		level = new IntegerVertexMarker(graph);
		return this;
	}

	public SearchAlgorithm withNumber() {
		checkStateForSettingParameters();
		number = new IntegerVertexMarker(graph);
		return this;
	}

	public SearchAlgorithm withParent() {
		checkStateForSettingParameters();
		parent = new ArrayVertexMarker<Edge>(graph);
		return this;

	}

	public IntFunction<Vertex> getInternalLevel() {
		return level;
	}

	public IntFunction<Vertex> getInternalNumber() {
		return number;
	}

	public Function<Vertex, Edge> getInternalParent() {
		return parent;
	}

	@Override
	public void disableOptionalResults() {
		checkStateForSettingParameters();
		level = null;
		number = null;
		parent = null;
	}

	@Override
	public void reset() {
		super.reset();
		vertexOrder = new Vertex[graph.getVCount() + 1];
		edgeOrder = new Edge[graph.getECount() + 1];
		visitedVertices = new BitSetVertexMarker(graph);
		visitedEdges = new BitSetEdgeMarker(graph);
		level = level == null ? null : new IntegerVertexMarker(graph);
		number = number == null ? null : new IntegerVertexMarker(graph);
		parent = parent == null ? null : new ArrayVertexMarker<Edge>(graph);

		num = 1;
		eNum = 1;
		// reset visitors (in subclass)
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		this.navigable = null;
		this.searchDirection = DEFAULT_SEARCH_DIRECTION;
	}

	/**
	 * @return the algorithm result <code>visitedVertices</code>.
	 */
	public BooleanFunction<Vertex> getVisitedVertices() {
		return visitedVertices;
	}

	/**
	 * @return the algorithm result <code>visitedEdges</code>.
	 */
	public BooleanFunction<Edge> getVisitedEdges() {
		return visitedEdges;
	}

	/**
	 * @return the intermediate result <code>edgeOrder</code>.
	 */
	public Edge[] getInternalEdgeOrder() {
		return edgeOrder;
	}

	/**
	 * @return the intermediate result <code>vertexOrder</code>.
	 */
	public Vertex[] getInternalVertexOrder() {
		return vertexOrder;
	}

	/**
	 * @return the intermediate value of <code>num</code>.
	 */
	public int getNum() {
		return num;
	}

	/**
	 * @return the intermediate value of <code>eNum</code>.
	 */
	public int getENum() {
		return eNum;
	}

	@Override
	public Permutation<Edge> getEdgeOrder() {
		checkStateForResult();
		return new ArrayPermutation<Edge>(edgeOrder);
	}

	@Override
	public Permutation<Vertex> getVertexOrder() {
		checkStateForResult();
		return new ArrayPermutation<Vertex>(vertexOrder);
	}

	/**
	 * Sets the search direction to "OUT" if <code>directed == true</code> and
	 * to "INOUT" otherwise. For searching backwards, use
	 * <code>setSearchDirection</code>.
	 */
	@Override
	public void setDirected(boolean directed) {
		checkStateForSettingParameters();
		searchDirection = directed ? EdgeDirection.OUT : EdgeDirection.INOUT;
	}

	@Override
	public boolean isHybrid() {
		return true;
	}

	/**
	 * Only returns <code>false</code> if <code>edgeDirection</code> is set to
	 * "INOUT".
	 */
	@Override
	public boolean isDirected() {
		return searchDirection != EdgeDirection.INOUT;
	}

	/**
	 * Sets the search direction to the given value. If "INOUT" is given, the
	 * algorithm interprets the graph as undirected graph.
	 * 
	 * @param searchDirection
	 *            the search direction this search algorithm uses.
	 */
	public void setSearchDirection(EdgeDirection searchDirection) {
		checkStateForSettingParameters();
		this.searchDirection = searchDirection;
	}

	/**
	 * @return the current search direction of the algorithm.
	 */
	public EdgeDirection getSearchDirection() {
		return searchDirection;
	}

	@Override
	protected void done() {
		if (state != AlgorithmStates.CANCELED) {
			state = num < graph.getVCount() + 1 ? AlgorithmStates.STOPPED
					: AlgorithmStates.FINISHED;
		}
	}

	public IntFunction<Vertex> getLevel() {
		checkStateForResult();
		return level;
	}

	public IntFunction<Vertex> getNumber() {
		checkStateForResult();
		return number;
	}

	public Function<Vertex, Edge> getParent() {
		checkStateForResult();
		return parent;
	}

	@Override
	public SearchAlgorithm execute() {
		for (Vertex currentRoot : graph.vertices()) {
			execute(currentRoot);
		}
		assert (state == AlgorithmStates.FINISHED);
		return this;
	}
}
