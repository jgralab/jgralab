package de.uni_koblenz.jgralab.algolib.algorithms;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public abstract class GraphAlgorithm {

	/**
	 * This is the default value for <code>subgraph</code>. The default subgraph
	 * is the whole graph. It returns <code>true</code> for all graph elements.
	 */
	public static final BooleanFunction<GraphElement> DEFAULT_SUBGRAPH = new BooleanFunction<GraphElement>() {

		@Override
		public boolean get(GraphElement parameter) {
			return true;
		}

		@Override
		public boolean isDefined(GraphElement parameter) {
			return true;
		}

		@Override
		public void set(GraphElement parameter, boolean value) {
			throw new UnsupportedOperationException(
					"This function is immutable.");
		}

	};

	/**
	 * The graph this graph algorithm works on.
	 */
	protected Graph graph;

	/**
	 * The subgraph this graph algorithm works on.
	 */
	protected BooleanFunction<GraphElement> subgraph;

	/**
	 * The state of this graph algorithm.
	 */
	protected AlgorithmStates state;

	/**
	 * Creates a new <code>GraphAlgorithm</code> for the given
	 * <code>graph</code>.
	 * 
	 * @param graph
	 *            the graph this algorithm works on.
	 */
	public GraphAlgorithm(Graph graph) {
		super();
		this.graph = graph;
		this.state = AlgorithmStates.INITIALIZED;
		resetParameters();
		reset();
	}

	/**
	 * Creates a new <code>GraphAlgorithm</code> for the given
	 * <code>graph</code> and sets the algorithm parameter <code>subgraph</code>
	 * to the given value of <code>subgraph</code>.
	 * 
	 * @param graph
	 *            the graph this algorithm works on.
	 * @param subgraph
	 *            the subgraph this algorithm works on.
	 */
	public GraphAlgorithm(Graph graph, BooleanFunction<GraphElement> subgraph) {
		this(graph);
		this.subgraph = subgraph;
	}

	/**
	 * Reinitializes all runtime variables and sets the algorithm state to
	 * <code>INITIALIZED</code>.
	 * 
	 * @throws IllegalStateException
	 *             if this algorithm is in state <code>RUNNING</code>.
	 */
	public void reset() {
		if (getState() != AlgorithmStates.RUNNING) {
			this.state = AlgorithmStates.INITIALIZED;
		} else {
			throw new IllegalStateException(
					"The algorithm may not be reseted while it is running.");
		}
	}

	public abstract void disableOptionalResults();

	/**
	 * Assigns the default values to all parameters.
	 * 
	 * @throws IllegalStateException
	 *             if this algorithm is not in state <code>INITIALIZED</code>.
	 */
	public void resetParameters() {
		if (getState() == AlgorithmStates.INITIALIZED) {
			this.subgraph = DEFAULT_SUBGRAPH;
			disableOptionalResults();
		} else {
			throw new IllegalStateException(
					"The parameters may only be reseted to their default values when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

	public Graph getGraph() {
		return graph;
	}

	/**
	 * Assigns a new graph to this algorithm object.
	 * 
	 * @param graph
	 *            the new graph this algorithm should work on.
	 * @throws IllegalStateExcetpion
	 *             if this algorithm is not in state <code>INITIALIZED</code>.
	 */
	public void setGraph(Graph graph) {
		if (getState() == AlgorithmStates.INITIALIZED) {
			this.graph = graph;
		} else {
			throw new IllegalStateException(
					"The graph may only be changed when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

	public BooleanFunction<GraphElement> getSubgraph() {
		return subgraph;
	}

	/**
	 * Assigns a new subgraph to this algorithm object.
	 * 
	 * @param subgraph
	 *            the new subgraph this algorithm should work on.
	 * @throws IllegalStateExcetpion
	 *             if this algorithm is not in state <code>INITIALIZED</code>.
	 */
	public void setSubgraph(BooleanFunction<GraphElement> subgraph) {
		if (getState() == AlgorithmStates.INITIALIZED) {
			this.subgraph = subgraph;
		} else {
			throw new IllegalStateException(
					"The subgraph may only be changed when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

	public synchronized AlgorithmStates getState() {
		return state;
	}

	/**
	 * Terminates the algorithm from inside by throwing an exception.
	 * 
	 * @throws AlgorithmTerminatedException
	 *             as default behavior
	 * @throws IllegalStateException
	 *             if this algorithm is not in state <code>RUNNING</code>.
	 */
	public void terminate() {
		if (getState() == AlgorithmStates.RUNNING) {
			done();
			throw new AlgorithmTerminatedException("Terminated by algorithm.");
		} else {
			throw new IllegalStateException(
					"The algorithm may only be terminated, when in state "
							+ AlgorithmStates.RUNNING);
		}
	}

	/**
	 * Checks if this algorithm was terminated from outside by interrupting the
	 * current thread. If this is the case, it changes the state to
	 * <code>CANCELED</code> and terminates the algorithm by throwing an
	 * exception.
	 * 
	 * @throws AlgorithmTerminatedException
	 *             if the current thread was interrupted
	 */
	protected synchronized void cancelIfInterrupted() {
		if (Thread.interrupted()) {
			state = AlgorithmStates.CANCELED;
			throw new AlgorithmTerminatedException("Thread interrupted.");
		}
	}

	/**
	 * This method sets the state to <code>RUNNING</code>. It must be called by
	 * the execute method when the actual algorithm starts running.
	 * 
	 * @throws IllegalStateException
	 *             if this algorithm is not in state <code>INITIALIZED</code> or
	 *             <code>STOPPED</code>.
	 */
	protected void startRunning() {
		if (state == AlgorithmStates.INITIALIZED
				|| state == AlgorithmStates.STOPPED) {
			state = AlgorithmStates.RUNNING;
		} else {
			throw new IllegalStateException(
					"The algorithm cannot be started, when in state " + state);
		}
	}

	/**
	 * This method sets the state of the algorithm after it is done to either
	 * <code>STOPPED</code> if a re-invocation is feasible or
	 * <code>FINISHED</code> if not. It must be called by the execute method
	 * when the actual algorithm stops running.
	 */
	protected abstract void done();

	/**
	 * Tells if this algorithm works on a directed graph.
	 * 
	 * @return <code>true</code> if the graph this algorithm works on is treated
	 *         as a directed graph, <code>false</code> otherwise.
	 */
	public abstract boolean isDirected();

	/**
	 * Adds a visitor to this algorithm.
	 * 
	 * @param visitor
	 *            the visitor to add to this algorithm
	 * @throws IllegalArgumentException
	 *             if the given <code>visitor</code> is incompatible with this
	 *             algorithm.
	 */
	public abstract void addVisitor(Visitor visitor);

	public void checkStateForResult() {
		if (state != AlgorithmStates.FINISHED
				&& state != AlgorithmStates.STOPPED) {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ state);
		}
	}

	public void checkStateForSettingParameters() {
		if (getState() != AlgorithmStates.INITIALIZED) {
			throw new IllegalStateException(
					"Parameters may only be changed when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

}
