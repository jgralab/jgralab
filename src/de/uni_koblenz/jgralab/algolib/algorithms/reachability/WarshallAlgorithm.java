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
package de.uni_koblenz.jgralab.algolib.algorithms.reachability;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.ArrayBinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.ArrayRelation;
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.functions.Relation;
import de.uni_koblenz.jgralab.algolib.problems.ReachabilitySolver;
import de.uni_koblenz.jgralab.algolib.problems.SimplePathsSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class WarshallAlgorithm extends StructureOrientedAlgorithm implements
		ReachabilitySolver, SimplePathsSolver {

	private IntFunction<Vertex> indexMapping;
	private Permutation<Vertex> vertexOrder;
	private int vertexCount;
	private boolean[][] reachable;
	private Edge[][] successor;

	public WarshallAlgorithm(Graph graph) {
		this(graph, null);
	}

	public WarshallAlgorithm(Graph graph, BooleanFunction<Edge> navigable) {
		super(graph, navigable);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		throw new UnsupportedOperationException(
				"This algorithm does not support any visitors.");
	}

	@Override
	public void disableOptionalResults() {
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public WarshallAlgorithm normal() {
		super.normal();
		return this;
	}

	@Override
	public WarshallAlgorithm reversed() {
		super.reversed();
		return this;
	}

	@Override
	public WarshallAlgorithm undirected() {
		super.undirected();
		return this;
	}

	@Override
	public boolean isHybrid() {
		return true;
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		throw new UnsupportedOperationException(
				"This algorithm does not support any visitors.");
	}

	@Override
	public void reset() {
		super.reset();
		SearchAlgorithm search = new BreadthFirstSearch(graph, null)
				.withNumber();
		search.setTraversalDirection(traversalDirection);
		try {
			search.execute();
		} catch (AlgorithmTerminatedException e) {
		}
		assert search.getState() == AlgorithmStates.FINISHED;
		indexMapping = search.getNumber();
		vertexOrder = search.getVertexOrder();
		vertexCount = graph.getVCount();
		reachable = new boolean[vertexCount + 1][vertexCount + 1];
		successor = new Edge[vertexCount + 1][vertexCount + 1];
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		traversalDirection = EdgeDirection.OUT;
	}

	@Override
	public WarshallAlgorithm execute() throws AlgorithmTerminatedException {
		startRunning();

		// clear and initialize arrays
		int length = vertexCount + 1;
		for (int vId = 1; vId < length; vId++) {
			for (int wId = 1; wId < length; wId++) {
				successor[vId][wId] = null;
			}
			reachable[vId][vId] = true;
		}
		for (Edge e : graph.edges()) {
			if ((navigable != null) && !navigable.get(e)) {
				continue;
			}
			int vId = indexMapping.get(e.getAlpha());
			int wId = indexMapping.get(e.getOmega());
			switch (traversalDirection) {
			case OUT:
				reachable[vId][wId] = true;
				successor[vId][wId] = e;
				break;
			case INOUT:
				reachable[vId][wId] = true;
				reachable[wId][vId] = true;
				successor[vId][wId] = e;
				successor[wId][vId] = e.getReversedEdge();
				break;
			case IN:
				reachable[wId][vId] = true;
				successor[wId][vId] = e.getReversedEdge();
			}
		}

		// main loop
		for (int vId = 1; vId <= vertexCount; vId++) {
			for (int uId = 1; uId <= vertexCount; uId++) {
				for (int wId = 1; wId <= vertexCount; wId++) {
					if (reachable[uId][vId] && reachable[vId][wId]
							&& !reachable[uId][wId]) {
						cancelIfInterrupted();
						reachable[uId][wId] = true;
						successor[uId][wId] = successor[uId][vId];
					}
				}
			}
		}

		done();
		return this;
	}

	@Override
	public Relation<Vertex, Vertex> getReachable() {
		checkStateForResult();
		return new ArrayRelation<>(reachable, indexMapping);
	}

	@Override
	public BinaryFunction<Vertex, Vertex, Edge> getSuccessor() {
		return new ArrayBinaryFunction<>(successor, indexMapping);
	}

	public Permutation<Vertex> getVertexOrder() {
		return vertexOrder;
	}

	public IntFunction<Vertex> getIndexMapping() {
		return indexMapping;
	}

	public boolean[][] getInternalReachable() {
		return reachable;
	}

	public Edge[][] getInternalSuccessor() {
		return successor;
	}

}
