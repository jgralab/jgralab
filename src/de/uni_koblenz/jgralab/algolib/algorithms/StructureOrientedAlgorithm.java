/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.TraversalSolver;

public abstract class StructureOrientedAlgorithm extends GraphAlgorithm implements
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

	public StructureOrientedAlgorithm(Graph graph) {
		this(graph, null, null);
	}

	public StructureOrientedAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph);
		this.navigable = navigable;
	}

	@Override
	public void setNavigable(BooleanFunction<Edge> navigable) {
		checkStateForSettingParameters();
		this.navigable = navigable;
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
		if (!isHybrid()) {
			if (isDirected() && traversalDirection == EdgeDirection.INOUT) {
				throw new UnsupportedOperationException(
						"This algorithm does not support undirected graphs.");
			} else if (!isDirected()
					&& traversalDirection != EdgeDirection.INOUT) {
				throw new UnsupportedOperationException(
						"This algorithm does not support directed graphs.");
			}
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
	public StructureOrientedAlgorithm normal() {
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
	public StructureOrientedAlgorithm reversed() {
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
	public StructureOrientedAlgorithm undirected() {
		setTraversalDirection(EdgeDirection.INOUT);
		return this;
	}

	@Override
	public boolean isDirected() {
		return traversalDirection != EdgeDirection.INOUT;
	}

}
