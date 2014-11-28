/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.algolib.algorithms;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.problems.ProblemSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public abstract class GraphAlgorithm implements ProblemSolver {

	/**
	 * The graph this graph algorithm works on.
	 */
	protected Graph graph;

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
		state = AlgorithmStates.INITIALIZED;
		resetParameters();
		reset();
	}

	public synchronized AlgorithmStates getState() {
		return state;
	}

	@Override
	public void setGraph(Graph graph) {
		checkStateForSettingParameters();
		this.graph = graph;
		reset();
	}

	public Graph getGraph() {
		return graph;
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
			state = AlgorithmStates.INITIALIZED;
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
