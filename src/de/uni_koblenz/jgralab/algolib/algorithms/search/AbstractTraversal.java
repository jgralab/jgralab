package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.TraversalSolver;

public abstract class AbstractTraversal extends GraphAlgorithm implements
		TraversalSolver {

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

	public AbstractTraversal(Graph graph) {
		this(graph, null, null);
	}

	public AbstractTraversal(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph);
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
		if (searchDirection == EdgeDirection.INOUT && !isHybrid()) {
			throw new UnsupportedOperationException(
					"This algorithm does not support undirected graphs.");

		}
		this.searchDirection = searchDirection;
	}

	public AbstractTraversal normal() {
		setSearchDirection(EdgeDirection.OUT);
		return this;
	}

	public AbstractTraversal reversed() {
		setSearchDirection(EdgeDirection.IN);
		return this;
	}

	public AbstractTraversal undirected() {
		setSearchDirection(EdgeDirection.INOUT);
		return this;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		this.navigable = null;
		this.searchDirection = DEFAULT_SEARCH_DIRECTION;
	}

	/**
	 * @return the current search direction of the algorithm.
	 */
	public EdgeDirection getSearchDirection() {
		return searchDirection;
	}

	public BooleanFunction<Edge> getNavigable() {
		return navigable;
	}

	@Override
	public void setNavigable(BooleanFunction<Edge> navigable) {
		if (getState() == AlgorithmStates.INITIALIZED) {
			this.navigable = navigable;
		} else {
			throw new IllegalStateException(
					"The edge navigability may only be changed when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

	@Override
	public boolean isDirected() {
		return searchDirection != EdgeDirection.INOUT;
	}

}