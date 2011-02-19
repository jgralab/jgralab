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
package de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.ArrayBinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.ArrayBinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.NegativeCyclesSolver;
import de.uni_koblenz.jgralab.algolib.problems.DistancesSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedProblemSolver;
import de.uni_koblenz.jgralab.algolib.problems.ShortestPathsSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class FloydAlgorithm extends StructureOrientedAlgorithm implements
		WeightedProblemSolver, DistancesSolver, ShortestPathsSolver,
		NegativeCyclesSolver {

	private IntFunction<Vertex> indexMapping;
	private Permutation<Vertex> vertexOrder;
	private int vertexCount;
	private double weightedDistance[][];
	private Edge[][] successor;
	private DoubleFunction<Edge> edgeWeight;
	private boolean negativeCycles;

	public FloydAlgorithm(Graph graph) {
		this(graph, null, null, null);
	}

	public FloydAlgorithm(Graph graph, BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable, DoubleFunction<Edge> edgeWeight) {
		super(graph, subgraph, navigable);
		this.edgeWeight = edgeWeight;
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
	public FloydAlgorithm normal() {
		super.normal();
		return this;
	}

	@Override
	public FloydAlgorithm reversed() {
		super.reversed();
		return this;
	}

	@Override
	public FloydAlgorithm undirected() {
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
	public void setEdgeWeight(DoubleFunction<Edge> edgeWeight) {
		checkStateForSettingParameters();
		this.edgeWeight = edgeWeight;
	}

	public DoubleFunction<Edge> getEdgeWeight() {
		return edgeWeight;
	}

	@Override
	public void reset() {
		super.reset();
		negativeCycles = false;
		SearchAlgorithm search = new BreadthFirstSearch(graph).withNumber();
		try {
			search.execute();
		} catch (AlgorithmTerminatedException e) {
		}
		assert search.getState() == AlgorithmStates.FINISHED;
		indexMapping = search.getNumber();
		vertexOrder = search.getVertexOrder();
		vertexCount = getVertexCount();
		weightedDistance = weightedDistance == null ? new double[vertexCount + 1][vertexCount + 1]
				: weightedDistance;
		successor = successor == null ? new Edge[vertexCount + 1][vertexCount + 1]
				: successor;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		traversalDirection = EdgeDirection.OUT;
	}

	@Override
	public FloydAlgorithm execute() throws AlgorithmTerminatedException {
		startRunning();

		// clear and initialize arrays
		int length = vertexCount + 1;
		for (int vId = 1; vId < length; vId++) {
			for (int wId = 1; wId < length; wId++) {
				weightedDistance[vId][wId] = Double.POSITIVE_INFINITY;
				successor[vId][wId] = null;
			}
			weightedDistance[vId][vId] = 0;
		}
		for (Edge e : graph.edges()) {
			if (subgraph != null && !subgraph.get(e) || navigable != null
					&& !navigable.get(e)) {
				continue;
			}
			int vId = indexMapping.get(e.getAlpha());
			int wId = indexMapping.get(e.getOmega());
			double newDistance = edgeWeight.get(e);
			switch (traversalDirection) {
			case OUT:
				if (weightedDistance[vId][wId] > newDistance) {
					weightedDistance[vId][wId] = newDistance;
					successor[vId][wId] = e;
				}
				break;
			case INOUT:
				if (weightedDistance[vId][wId] > newDistance) {
					weightedDistance[vId][wId] = newDistance;
					weightedDistance[wId][vId] = newDistance;
					successor[vId][wId] = e;
					successor[wId][vId] = e.getReversedEdge();
				}
				break;
			case IN:
				if (weightedDistance[wId][wId] > newDistance) {
					weightedDistance[wId][vId] = newDistance;
					successor[wId][vId] = e.getReversedEdge();
				}
			}
		}

		// main loop
		for (int vId = 1; vId <= vertexCount; vId++) {
			for (int uId = 1; uId <= vertexCount; uId++) {
				for (int wId = 1; wId <= vertexCount; wId++) {
					double newDistance = weightedDistance[uId][vId]
							+ weightedDistance[vId][wId];
					if (weightedDistance[uId][wId] > newDistance) {
						cancelIfInterrupted();
						weightedDistance[uId][wId] = newDistance;
						successor[uId][wId] = successor[uId][vId];
					}
					if (uId == wId && weightedDistance[uId][wId] < 0) {
						negativeCycles = true;
						terminate();
					}
				}
			}
		}

		done();
		return this;
	}

	@Override
	public BinaryDoubleFunction<Vertex, Vertex> getDistances() {
		checkStateForResult();
		return new ArrayBinaryDoubleFunction<Vertex>(weightedDistance,
				indexMapping);
	}

	@Override
	public BinaryFunction<Vertex, Vertex, Edge> getSuccessor() {
		checkStateForResult();
		return new ArrayBinaryFunction<Vertex, Edge>(successor, indexMapping);
	}

	public Permutation<Vertex> getVertexOrder() {
		return vertexOrder;
	}

	public IntFunction<Vertex> getIndexMapping() {
		return indexMapping;
	}

	public double[][] getInternalWeightedDistance() {
		return weightedDistance;
	}

	public Edge[][] getInternalSuccessor() {
		return successor;
	}

	@Override
	public boolean hasNegativeCycles() {
		checkStateForResult();
		return negativeCycles;
	}

	public boolean getInternalNegativeCycles() {
		return negativeCycles;
	}
}
