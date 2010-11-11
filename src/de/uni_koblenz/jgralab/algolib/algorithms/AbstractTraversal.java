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
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.TraversalSolver;

public abstract class AbstractTraversal extends GraphAlgorithm implements
		TraversalSolver {

	/**
	 * This is the default value for the parameter <code>searchDirection</code>.
	 * By default the algorithm follows only outgoing edges, which also means
	 * that the graph is interpreted as a directed graph.
	 */
	public static final EdgeDirection DEFAULT_TRAVERSAL_DIRECTION = EdgeDirection.OUT;
	/**
	 * A function that tells if a reachable edge is also navigable.
	 */
	protected BooleanFunction<Edge> navigable;
	/**
	 * The search direction this search algorithm uses.
	 */
	protected EdgeDirection traversalDirection;

	public AbstractTraversal(Graph graph) {
		this(graph, null, null);
	}

	public AbstractTraversal(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph);
		this.navigable = navigable;
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

	/**
	 * Sets the search direction to the given value. If "INOUT" is given, the
	 * algorithm interprets the graph as undirected graph.
	 * 
	 * @param traversalDirection
	 *            the search direction this search algorithm uses.
	 */
	public void setTraversalDirection(EdgeDirection traversalDirection) {
		checkStateForSettingParameters();
		if (traversalDirection == EdgeDirection.INOUT && !isHybrid()) {
			throw new UnsupportedOperationException(
					"This algorithm does not support undirected graphs.");

		}
		this.traversalDirection = traversalDirection;
	}
	
	/**
	 * @return the current search direction of the algorithm.
	 */
	public EdgeDirection getTraversalDirection() {
		return traversalDirection;
	}

	public BooleanFunction<Edge> getNavigable() {
		return navigable;
	}
	
	@Override
	public void resetParameters() {
		super.resetParameters();
		this.navigable = null;
		this.traversalDirection = DEFAULT_TRAVERSAL_DIRECTION;
	}

	/**
	 * If this method is called before executing the algorithm, the graph will
	 * be traversed in normal order with respect to the edges's normal
	 * direction.
	 * 
	 * @return this algorithm object.
	 */
	public AbstractTraversal normal() {
		setTraversalDirection(EdgeDirection.OUT);
		return this;
	}

	/**
	 * If this method is called before executing the algorithm, the graph will
	 * be traversed in reversed order with respect to the edge's reversed
	 * direction.
	 * 
	 * @return this algorithm object.
	 */
	public AbstractTraversal reversed() {
		setTraversalDirection(EdgeDirection.IN);
		return this;
	}

	/**
	 * If this method is called before executing the algorithm, the graph will
	 * be treated as undirected graph and the edges will be followed either with
	 * respect to their normal order or their reversed order.
	 * 
	 * @return this algorithm object.
	 */
	public AbstractTraversal undirected() {
		setTraversalDirection(EdgeDirection.INOUT);
		return this;
	}

	@Override
	public boolean isDirected() {
		return traversalDirection != EdgeDirection.INOUT;
	}

}
