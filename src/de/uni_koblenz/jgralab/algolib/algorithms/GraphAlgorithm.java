/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralab.algolib.algorithms;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.ProblemSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public abstract class GraphAlgorithm implements ProblemSolver {

	/**
	 * The graph this graph algorithm works on.
	 */
	protected Graph graph;

	private int vertexCount;

	private int edgeCount;

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

	public synchronized AlgorithmStates getState() {
		return state;
	}

	@Override
	public void setGraph(Graph graph) {
		checkStateForSettingParameters();
		this.graph = graph;
		vertexCount = -1;
		edgeCount = -1;
	}

	@Override
	public void setSubgraph(BooleanFunction<GraphElement> subgraph) {
		checkStateForSettingParameters();
		this.subgraph = subgraph;
		vertexCount = -1;
		edgeCount = -1;
	}

	public Graph getGraph() {
		return graph;
	}

	public BooleanFunction<GraphElement> getSubgraph() {
		return subgraph;
	}

	public int getVertexCount() {
		if (vertexCount < 0) {
			if (subgraph == null) {
				vertexCount = graph.getVCount();
			} else {
				vertexCount = 0;
				for (Vertex currentVertex : graph.vertices()) {
					if (subgraph.get(currentVertex)) {
						vertexCount++;
					}
				}
			}
		}
		return vertexCount;
	}

	public int getEdgeCount() {
		if (edgeCount < 0) {
			if (subgraph == null) {
				edgeCount = graph.getECount();
			} else {
				edgeCount = 0;
				for (Edge currentEdge : graph.edges()) {
					if (subgraph.get(currentEdge)) {
						edgeCount++;
					}
				}
			}
		}
		return edgeCount;
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

	/**
	 * Assigns the default values to all parameters.
	 * 
	 * @throws IllegalStateException
	 *             if this algorithm is not in state <code>INITIALIZED</code>.
	 */
	public void resetParameters() {
		checkStateForSettingParameters();
		this.subgraph = null;
		vertexCount = -1;
		edgeCount = -1;
		disableOptionalResults();
	}

	/**
	 * Checks the state of this algorithm object and throws an exception if
	 * results cannot be retrieved now.
	 * 
	 * @throws IllegalStateException
	 *             if not in state <code>STOPPED</code> or <code>FINISHED</code>
	 *             .
	 */
	public void checkStateForResult() {
		if (state != AlgorithmStates.FINISHED
				&& state != AlgorithmStates.STOPPED) {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ state);
		}
	}

	/**
	 * Checks the state of this algorithm object and throws an exception if
	 * parameters cannot be changed now.
	 * 
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public void checkStateForSettingParameters() {
		if (getState() != AlgorithmStates.INITIALIZED) {
			throw new IllegalStateException(
					"Parameters may not be changed while in state " + state);
		}
	}

	/**
	 * Checks the state of this algorithm object and throws an exception if
	 * visitors cannot be modified now.
	 * 
	 * @throws IllegalStateException
	 *             if in state <code>RUNNING</code> or <code>CANCELED</code>.
	 */
	public void checkStateForSettingVisitors() {
		if (getState() == AlgorithmStates.RUNNING
				|| getState() == AlgorithmStates.CANCELED) {
			throw new IllegalStateException(
					"Parameters may not be changed while in state " + state);
		}
	}

	public abstract void disableOptionalResults();

	/**
	 * Terminates the algorithm from inside by throwing an exception.
	 * 
	 * @throws AlgorithmTerminatedException
	 *             as default behavior
	 * @throws IllegalStateException
	 *             if this algorithm is not in state <code>RUNNING</code>.
	 */
	public void terminate() throws AlgorithmTerminatedException {
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
	protected synchronized void cancelIfInterrupted()
			throws AlgorithmTerminatedException {
		if (Thread.interrupted()) {
			state = AlgorithmStates.CANCELED;
			Thread.currentThread().interrupt();
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
	 * Tells if this algorithm can work on both, directed and undirected graphs.
	 * 
	 * @return true if this algorithm can work on directed and undirected
	 *         graphs.
	 */
	public abstract boolean isHybrid();

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

	/**
	 * Removes a visitor from this algorithm.
	 * 
	 * @param visitor
	 *            the visitor to be removed from this algorithm
	 */
	public abstract void removeVisitor(Visitor visitor);

}
