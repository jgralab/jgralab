package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.functions.ArrayPermutation;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.CompleteTraversalSolver;
import de.uni_koblenz.jgralab.algolib.problems.TraversalFromVertexSolver;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

/**
 * This is the superclass of all search algorithms. It handles the storage of
 * common attributes (e.g. searchDirection) and provides several common methods
 * required by all search algorithms.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public abstract class SearchAlgorithm extends AbstractTraversal implements
		TraversalFromVertexSolver, CompleteTraversalSolver {

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

	/**
	 * The intermediate optional result <code>level</code>.
	 */
	protected IntFunction<Vertex> level;

	/**
	 * The intermediate optional result <code>number</code>.
	 */
	protected IntFunction<Vertex> number;

	/**
	 * The intermediate optional result <code>parent</code>.
	 */
	protected Function<Vertex, Edge> parent;

	/**
	 * Creates a new search algorithm.
	 * 
	 * @param graph
	 *            the graph this search algorithm works on.
	 * @param subgraph
	 *            the subgraph function for this search algorithm.
	 * @param directed
	 *            the flag that tells whether this search algorithm should treat
	 *            the graph as directed or undirected algorithm.
	 * @param navigable
	 *            the navigable function for this search algorithm.
	 */
	public SearchAlgorithm(Graph graph, BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
	}

	/**
	 * Creates a new search algorithm.
	 * 
	 * @param graph
	 *            the graph this search algorithm works on.
	 */
	public SearchAlgorithm(Graph graph) {
		this(graph, null, null);
	}

	/**
	 * Activates the computation of the optional result <code>level</code>.
	 * 
	 * @return this <code>SearchAlgorithm</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public SearchAlgorithm withLevel() {
		checkStateForSettingParameters();
		level = new IntegerVertexMarker(graph);
		return this;
	}

	/**
	 * Deactivaces the computation of the optional result <code>level</code>.
	 * 
	 * @return this <code>SearchAlgorithm</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public SearchAlgorithm withoutLevel() {
		checkStateForSettingParameters();
		level = null;
		return this;
	}

	/**
	 * Activates the computation of the optional result <code>number</code>.
	 * 
	 * @return this <code>SearchAlgorithm</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public SearchAlgorithm withNumber() {
		checkStateForSettingParameters();
		number = new IntegerVertexMarker(graph);
		return this;
	}

	/**
	 * Deactivates the computation of the optional result <code>number</code>.
	 * 
	 * @return this <code>SearchAlgorithm</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public SearchAlgorithm withoutNumber() {
		checkStateForSettingParameters();
		number = null;
		return this;
	}

	/**
	 * Activates the computation of the optional result <code>parent</code>.
	 * 
	 * @return this <code>SearchAlgorithm</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public SearchAlgorithm withParent() {
		checkStateForSettingParameters();
		parent = new ArrayVertexMarker<Edge>(graph);
		return this;

	}

	/**
	 * Deactivates the computation of the optional result <code>parent</code>.
	 * 
	 * @return this <code>SearchAlgorithm</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public SearchAlgorithm withoutParent() {
		checkStateForSettingParameters();
		parent = null;
		return this;
	}

	/**
	 * @return the internal representation of the optional result
	 *         <code>level</code>.
	 */
	public IntFunction<Vertex> getInternalLevel() {
		return level;
	}

	/**
	 * @return the internal representation of the optional result
	 *         <code>number</code>.
	 */
	public IntFunction<Vertex> getInternalNumber() {
		return number;
	}

	/**
	 * @return the internal representation of the optional result
	 *         <code>parent</code>.
	 */
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
		vertexOrder = new Vertex[getVertexCount() + 1];
		edgeOrder = new Edge[getEdgeCount() + 1];
		visitedVertices = new BitSetVertexMarker(graph);
		visitedEdges = new BitSetEdgeMarker(graph);
		level = level == null ? null : new IntegerVertexMarker(graph);
		number = number == null ? null : new IntegerVertexMarker(graph);
		parent = parent == null ? null : new ArrayVertexMarker<Edge>(graph);

		num = 1;
		eNum = 1;
		// reset visitors (in subclass)
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
	 * @return the internal representation of the result <code>edgeOrder</code>.
	 */
	public Edge[] getInternalEdgeOrder() {
		return edgeOrder;
	}

	/**
	 * @return the internal representation of the result
	 *         <code>vertexOrder</code>.
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

	@Override
	protected void done() {
		if (state != AlgorithmStates.CANCELED) {
			state = num < getVertexCount() + 1 ? AlgorithmStates.STOPPED
					: AlgorithmStates.FINISHED;
		}
	}

	/**
	 * Retrieves the optional result <code>level</code>.
	 * 
	 * @return the optional result <code>level</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>STOPPED</code> or <code>FINISHED</code>
	 *             .
	 */
	public IntFunction<Vertex> getLevel() {
		checkStateForResult();
		return level;
	}

	/**
	 * Retrieves the optional result <code>number</code>.
	 * 
	 * @return the optional result <code>number</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>STOPPED</code> or <code>FINISHED</code>
	 *             .
	 */
	public IntFunction<Vertex> getNumber() {
		checkStateForResult();
		return number;
	}

	/**
	 * Retrieves the optional result <code>parent</code>.
	 * 
	 * @return the optional result <code>parent</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>STOPPED</code> or <code>FINISHED</code>
	 *             .
	 */
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
