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

import java.util.LinkedList;
import java.util.Queue;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.problems.DistancesFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.ShortestPathsFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.TraversalSolver;
import de.uni_koblenz.jgralab.algolib.problems.DistanceFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedProblemSolver;
import de.uni_koblenz.jgralab.algolib.problems.ShortestPathFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.DoubleVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class FordMooreAlgorithm extends StructureOrientedAlgorithm implements
		WeightedProblemSolver, TraversalSolver, DistancesFromVertexSolver,
		ShortestPathsFromVertexSolver,
		DistanceFromVertexToVertexSolver,
		ShortestPathFromVertexToVertexSolver {

	private DoubleFunction<Edge> edgeWeight;
	private Vertex target;

	private Function<Vertex, Edge> parent;
	private DoubleFunction<Vertex> distance;

	private Queue<Vertex> vertexQueue;
	private IntFunction<Vertex> pushCount;
	private int maxPushCount;
	private boolean negativeCycleDetected;

	public FordMooreAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable, DoubleFunction<Edge> weight) {
		super(graph, subgraph, navigable);
		this.edgeWeight = weight;
	}

	public FordMooreAlgorithm(Graph graph) {
		this(graph, null, null, null);
	}

	@Override
	public void setEdgeWeight(DoubleFunction<Edge> edgeWeight) {
		checkStateForSettingParameters();
		this.edgeWeight = edgeWeight;
	}

	@Override
	public void disableOptionalResults() {
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public FordMooreAlgorithm normal() {
		super.normal();
		return this;
	}

	@Override
	public FordMooreAlgorithm reversed() {
		super.reversed();
		return this;
	}

	@Override
	public FordMooreAlgorithm undirected() {
		super.undirected();
		return this;
	}

	@Override
	public boolean isHybrid() {
		return true;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		throw new UnsupportedOperationException(
				"This algorithm currently doesn't support visitors!");
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		throw new UnsupportedOperationException(
				"This algorithm currently doesn't support visitors!");
	}

	@Override
	public void reset() {
		super.reset();
		parent = new ArrayVertexMarker<Edge>(graph);
		distance = new DoubleVertexMarker(graph);
		for (Vertex v : graph.vertices()) {
			distance.set(v, Double.POSITIVE_INFINITY);
		}
		vertexQueue = vertexQueue == null ? new LinkedList<Vertex>()
				: vertexQueue;
		vertexQueue.clear();
		pushCount = new IntegerVertexMarker(graph);
		maxPushCount = getVertexCount() - 1;
		negativeCycleDetected = false;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		edgeWeight = null;
		traversalDirection = EdgeDirection.OUT;
	}

	@Override
	public FordMooreAlgorithm execute(Vertex start)
			throws AlgorithmTerminatedException {
		if (subgraph != null && !subgraph.get(start)) {
			throw new IllegalArgumentException("Start vertex not in subgraph!");
		}
		startRunning();
		for (Vertex currentVertex : graph.vertices()) {
			if (subgraph == null || subgraph.get(currentVertex)) {
				pushCount.set(currentVertex, 0);
			}
		}
		distance.set(start, 0.0);
		vertexQueue.add(start);
		pushCount.set(start, pushCount.get(start) + 1);

		while (!vertexQueue.isEmpty()) {
			Vertex currentVertex = vertexQueue.poll();
			assert (currentVertex != null);
			for (Edge currentEdge : currentVertex
					.incidences(traversalDirection)) {
				cancelIfInterrupted();
				if (subgraph != null && !subgraph.get(currentEdge)
						|| navigable != null && !navigable.get(currentEdge)) {
					continue;
				}
				Vertex nextVertex = currentEdge.getThat();
				assert (subgraph.get(nextVertex));
				double newDistance = distance.get(currentVertex)
						+ (edgeWeight == null ? 1.0 : edgeWeight
								.get(currentEdge));
				if (newDistance < distance.get(nextVertex)) {
					parent.set(nextVertex, currentEdge);
					distance.set(nextVertex, newDistance);
					int newCount = pushCount.get(nextVertex) + 1;
					if (newCount > maxPushCount) {
						negativeCycleDetected = true;
						terminate();
					}
					pushCount.set(nextVertex, newCount);
					vertexQueue.add(nextVertex);
				}
			}
		}
		done();
		return this;
	}

	@Override
	public FordMooreAlgorithm execute(Vertex start, Vertex target)
			throws AlgorithmTerminatedException {
		if (subgraph != null && !subgraph.get(target)) {
			throw new IllegalArgumentException("Target vertex not in subgraph!");
		}
		this.target = target;
		return execute(start);
	}

	@Override
	public DoubleFunction<Vertex> getDistance() {
		checkStateForResult();
		return distance;
	}

	public DoubleFunction<Vertex> getInternalDistance() {
		return distance;
	}

	@Override
	public double getDistanceToTarget() {
		checkStateForResult();
		if (target != null) {
			return distance.get(target);
		}
		throw new UnsupportedOperationException(
				"No target vertex specified or wrong execute method used.");
	}

	@Override
	public Function<Vertex, Edge> getParent() {
		checkStateForResult();
		return parent;
	}

	public Function<Vertex, Edge> getInternalParent() {
		return parent;
	}

	public boolean hasNegativeCycleDetected() {
		return negativeCycleDetected;
	}

	public int getMaxPushCount() {
		return maxPushCount;
	}

	public Queue<Vertex> getVertexQueue() {
		return vertexQueue;
	}

	public IntFunction<Vertex> getPushCount() {
		return pushCount;
	}

}
