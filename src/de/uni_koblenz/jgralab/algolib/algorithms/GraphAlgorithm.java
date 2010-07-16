package de.uni_koblenz.jgralab.algolib.algorithms;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

public abstract class GraphAlgorithm {

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

	protected Graph graph;
	protected BooleanFunction<GraphElement> subgraph;
	protected AlgorithmStates state;
	protected boolean terminated;

	public GraphAlgorithm(Graph graph) {
		super();
		this.graph = graph;
		this.state = AlgorithmStates.INITIALIZED;
		resetParameters();
		reset();
	}

	public void reset() {
		if (getState() != AlgorithmStates.RUNNING) {
			this.terminated = false;
			this.state = AlgorithmStates.INITIALIZED;
		} else {
			throw new IllegalStateException(
					"The algorithm may not be reseted while it is running.");
		}
	}

	public void resetParameters() {
		if (getState() == AlgorithmStates.INITIALIZED) {
			this.subgraph = DEFAULT_SUBGRAPH;
		} else {
			throw new IllegalStateException(
					"The parameters may only be reseted to their default values when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

	public GraphAlgorithm(Graph graph, BooleanFunction<GraphElement> subgraph) {
		this(graph);
		this.subgraph = subgraph;
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		if (getState() == AlgorithmStates.INITIALIZED) {
			this.graph = graph;
			reset();
		} else {
			throw new IllegalStateException(
					"The graph may only be changed when in state "
							+ AlgorithmStates.INITIALIZED);
		}
	}

	public BooleanFunction<GraphElement> getSubgraph() {
		return subgraph;
	}

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

	public void terminate() {
		if (getState() == AlgorithmStates.RUNNING) {
			terminated = true;
		} else {
			throw new IllegalStateException(
					"The algorithm may only be terminated, when in state "
							+ AlgorithmStates.RUNNING);
		}
	}

	protected synchronized void cancelIfInterrupted() {
		if (Thread.interrupted()) {
			state = AlgorithmStates.CANCELED;
		}
	}

	public abstract boolean isDirected();
}
