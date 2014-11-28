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
package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
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
import de.uni_koblenz.jgralab.graphmarker.IntegerEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

/**
 * This is the superclass of all search algorithms. It handles the storage of
 * common attributes (e.g. searchDirection) and provides several common methods
 * required by all search algorithms.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public abstract class SearchAlgorithm extends StructureOrientedAlgorithm
		implements TraversalFromVertexSolver, CompleteTraversalSolver {

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
	 * The intermediate optional result <code>enumber</code>.
	 */
	protected IntFunction<Edge> enumber;

	/**
	 * The intermediate optional result <code>parent</code>.
	 */
	protected Function<Vertex, Edge> parent;

	/**
	 * Creates a new search algorithm.
	 * 
	 * @param graph
	 *            the graph this search algorithm works on.
	 * @param navigable
	 *            the navigable function for this search algorithm.
	 */
	public SearchAlgorithm(Graph graph, BooleanFunction<Edge> navigable) {
		super(graph, navigable);
	}

	/**
	 * Creates a new search algorithm.
	 * 
	 * @param graph
	 *            the graph this search algorithm works on.
	 */
	public SearchAlgorithm(Graph graph) {
		this(graph, null);
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
	 * Activates the computation of the optional result <code>enumber</code>.
	 * 
	 * @return this <code>SearchAlgorithm</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public SearchAlgorithm withENumber() {
		checkStateForSettingParameters();
		enumber = new IntegerEdgeMarker(graph);
		return this;
	}

	/**
	 * Deactivates the computation of the optional result <b>enumber</b>.
	 * 
	 * @return this <code>SearchAlgorithm</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public SearchAlgorithm withoutENumber() {
		checkStateForSettingParameters();
		enumber = null;
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
		parent = new ArrayVertexMarker<>(graph);
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

	@Override
	public void disableOptionalResults() {
		checkStateForSettingParameters();
		level = null;
		number = null;
		enumber = null;
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
		enumber = enumber == null ? null : new IntegerEdgeMarker(graph);
		parent = parent == null ? null : new ArrayVertexMarker<Edge>(graph);
		num = 1;
		eNum = 1;
		// reset visitors (in subclass)
	}

	/**
	 * @return the internal representation of the result
	 *         <code>vertexOrder</code>.
	 */
	public Vertex[] getInternalVertexOrder() {
		return vertexOrder;
	}

	/**
	 * @return the internal representation of the result <code>edgeOrder</code>.
	 */
	public Edge[] getInternalEdgeOrder() {
		return edgeOrder;
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
	 *         <code>enumber</code>.
	 */
	public IntFunction<Edge> getInternalEnumber() {
		return enumber;
	}

	/**
	 * @return the internal representation of the optional result
	 *         <code>parent</code>.
	 */
	public Function<Vertex, Edge> getInternalParent() {
		return parent;
	}

	@Override
	public boolean isHybrid() {
		return true;
	}

	@Override
	public abstract SearchAlgorithm execute(Vertex root)
			throws AlgorithmTerminatedException;

	@Override
	public SearchAlgorithm execute() throws AlgorithmTerminatedException {
		if (graph.getVCount() > 0) {
			for (Vertex currentRoot : graph.vertices()) {
				execute(currentRoot);
				if (state == AlgorithmStates.FINISHED) {
					break;
				}
			}
		} else {
			done();
		}
		assert (state == AlgorithmStates.FINISHED);
		return this;
	}

	@Override
	protected void done() {
		if (state != AlgorithmStates.CANCELED) {
			state = num < graph.getVCount() + 1 ? AlgorithmStates.STOPPED
					: AlgorithmStates.FINISHED;
		}
	}

	@Override
	public Permutation<Vertex> getVertexOrder() {
		checkStateForResult();
		return new ArrayPermutation<>(vertexOrder);
	}

	@Override
	public Permutation<Edge> getEdgeOrder() {
		checkStateForResult();
		return new ArrayPermutation<>(edgeOrder);
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
	 * Retrieves the optional result <code>enumber</code>.
	 * 
	 * @return the optional result <code>enumber</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>STOPPED</code> or <code>FINISHED</code>
	 *             .
	 */
	public IntFunction<Edge> getEnumber() {
		checkStateForResult();
		return enumber;
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
}
