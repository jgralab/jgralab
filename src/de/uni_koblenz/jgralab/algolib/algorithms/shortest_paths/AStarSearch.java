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
package de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorList;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.problems.DistanceFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.ShortestPathFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedProblemSolver;
import de.uni_koblenz.jgralab.algolib.util.PriorityQueue;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorList;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.DoubleVertexMarker;

public class AStarSearch extends StructureOrientedAlgorithm implements
		DistanceFromVertexToVertexSolver, ShortestPathFromVertexToVertexSolver,
		WeightedProblemSolver {

	protected DoubleFunction<Vertex> weightedDistance;
	protected BooleanFunction<Vertex> visitedVertices;
	protected Function<Vertex, Edge> parent;

	protected DoubleFunction<Edge> edgeWeight;
	private BinaryDoubleFunction<Vertex, Vertex> heuristic;
	protected Vertex target;
	protected GraphVisitorAdapter targetVertexReachedVisitor;

	protected PriorityQueue<Vertex> vertexQueue;
	protected GraphVisitorList visitors;

	public AStarSearch(Graph graph, BooleanFunction<Edge> navigable,
			DoubleFunction<Edge> edgeWeight,
			BinaryDoubleFunction<Vertex, Vertex> heuristic) {
		super(graph, navigable);
		this.edgeWeight = edgeWeight;
		this.heuristic = heuristic;
	}

	public AStarSearch(Graph graph) {
		this(graph, null, null, null);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		visitor.setAlgorithm(this);
		visitors.addVisitor(visitor);
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		visitors.removeVisitor(visitor);
	}

	@Override
	public void disableOptionalResults() {
	}

	@Override
	public AStarSearch normal() {
		super.normal();
		return this;
	}

	@Override
	public AStarSearch reversed() {
		super.reversed();
		return this;
	}

	@Override
	public AStarSearch undirected() {
		super.undirected();
		return this;
	}

	@Override
	public boolean isHybrid() {
		return true;
	}

	@Override
	public void reset() {
		super.reset();
		visitors.reset();
		weightedDistance = new DoubleVertexMarker(graph);
		for (Vertex v : graph.vertices()) {
			weightedDistance.set(v, Double.POSITIVE_INFINITY);
		}
		visitedVertices = new BitSetVertexMarker(graph);
		parent = new ArrayVertexMarker<>(graph);
		vertexQueue = vertexQueue == null ? new PriorityQueue<Vertex>()
				: vertexQueue.clear();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new SearchVisitorList();
		targetVertexReachedVisitor = new SearchVisitorAdapter() {

			@Override
			public void visitVertex(Vertex v)
					throws AlgorithmTerminatedException {
				if (target == v) {
					terminate();
				}
			}

		};
	}

	@Override
	public void setEdgeWeight(DoubleFunction<Edge> edgeWeight) {
		checkStateForSettingParameters();
		this.edgeWeight = edgeWeight;
	}

	public void setHeuristic(BinaryDoubleFunction<Vertex, Vertex> heuristic) {
		checkStateForSettingParameters();
		this.heuristic = heuristic;
	}

	@Override
	public AStarSearch execute(Vertex start, Vertex target)
			throws AlgorithmTerminatedException {
		TraversalContext subgraph = graph.getTraversalContext();
		if (subgraph != null && !subgraph.containsVertex(target)) {
			throw new IllegalArgumentException("Target vertex not in subgraph!");
		}
		this.target = target;
		visitors.addVisitor(targetVertexReachedVisitor);

		internalExecute(start, target);

		visitors.removeVisitor(targetVertexReachedVisitor);
		return this;
	}

	protected void internalExecute(Vertex start, Vertex target)
			throws AlgorithmTerminatedException {
		TraversalContext subgraph = graph.getTraversalContext();
		if (subgraph != null && !subgraph.containsVertex(start)) {
			throw new IllegalArgumentException("Start vertex not in subgraph!");
		}
		startRunning();
		weightedDistance.set(start, 0);
		vertexQueue.put(start, 0);

		// main loop
		while (!vertexQueue.isEmpty()) {
			Vertex currentVertex = vertexQueue.getNext();
			if (!visitedVertices.get(currentVertex)) {

				visitors.visitVertex(currentVertex);
				visitedVertices.set(currentVertex, true);

				for (Edge currentEdge : currentVertex
						.incidences(traversalDirection)) {
					cancelIfInterrupted();
					if (navigable != null && !navigable.get(currentEdge)) {
						continue;
					}
					Vertex nextVertex = currentEdge.getThat();
					double newDistance = weightedDistance.get(currentVertex)
							+ (edgeWeight == null ? 1.0 : edgeWeight
									.get(currentEdge));

					visitors.visitEdge(currentEdge);

					if (weightedDistance.get(nextVertex) > newDistance) {
						parent.set(nextVertex, currentEdge);
						weightedDistance.set(nextVertex, newDistance);
						vertexQueue.put(
								nextVertex,
								newDistance
										+ (heuristic == null ? 0 : heuristic
												.get(nextVertex, target)));
					}
				}
			}
		}

		done();
	}

	@Override
	public void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public double getDistanceToTarget() {
		checkStateForResult();
		if (target != null) {
			return weightedDistance.get(target);
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

	public PriorityQueue<Vertex> getVertexQueue() {
		return vertexQueue;
	}

	public BooleanFunction<Vertex> visitedVertices() {
		return visitedVertices;
	}

	public DoubleFunction<Vertex> getDistance() {
		return weightedDistance;
	}
}
