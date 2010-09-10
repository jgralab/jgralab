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
package de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorComposition;
import de.uni_koblenz.jgralab.algolib.buffers.PriorityQueue;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.problems.WeightedDistanceFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedShortestPathFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorComposition;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.DoubleVertexMarker;

public class AStarSearch extends AbstractTraversal implements
		WeightedDistanceFromVertexToVertexSolver,
		WeightedShortestPathFromVertexToVertexSolver {

	protected DoubleFunction<Vertex> weightedDistance;
	protected BooleanFunction<Vertex> visitedVertices;
	protected Function<Vertex, Edge> parent;

	protected DoubleFunction<Edge> edgeWeight;
	private BinaryDoubleFunction<Vertex, Vertex> heuristic;
	protected Vertex target;
	protected GraphVisitorAdapter targetVertexReachedVisitor;

	protected PriorityQueue<Vertex> vertexQueue;
	protected GraphVisitorComposition visitors;

	public AStarSearch(Graph graph, BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable, DoubleFunction<Edge> edgeWeight,
			BinaryDoubleFunction<Vertex, Vertex> heuristic) {
		super(graph, subgraph, navigable);
		this.edgeWeight = edgeWeight;
		this.heuristic = heuristic;
	}

	public AStarSearch(Graph graph) {
		this(graph, null, null, null, null);
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
		parent = new ArrayVertexMarker<Edge>(graph);
		vertexQueue = vertexQueue == null ? new PriorityQueue<Vertex>()
				: vertexQueue.clear();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new SearchVisitorComposition();
		targetVertexReachedVisitor = new SearchVisitorAdapter() {

			@Override
			public void visitVertex(Vertex v) {
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
	public AStarSearch execute(Vertex start, Vertex target) {
		if (subgraph != null && !subgraph.get(target)) {
			throw new IllegalArgumentException("Target vertex not in subgraph!");
		}
		this.target = target;
		visitors.addVisitor(targetVertexReachedVisitor);

		internalExecute(start, target);

		visitors.removeVisitor(targetVertexReachedVisitor);
		return this;
	}

	protected void internalExecute(Vertex start, Vertex target) {
		if (subgraph != null && !subgraph.get(start)) {
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
						.incidences(searchDirection)) {
					cancelIfInterrupted();
					if (subgraph != null && !subgraph.get(currentEdge)
							|| navigable != null && !navigable.get(currentEdge)) {
						continue;
					}
					Vertex nextVertex = currentEdge.getThat();
					assert (subgraph == null || subgraph.get(nextVertex));
					double newDistance = weightedDistance.get(currentVertex)
							+ (edgeWeight == null ? 1.0 : edgeWeight
									.get(currentEdge));

					visitors.visitEdge(currentEdge);

					if (weightedDistance.get(nextVertex) > newDistance) {
						parent.set(nextVertex, currentEdge);
						weightedDistance.set(nextVertex, newDistance);
						vertexQueue.put(nextVertex, newDistance
								+ (heuristic == null ? 0 : heuristic.get(
										nextVertex, target)));
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
	public double getWeightedDistanceToTarget() {
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

	public DoubleFunction<Vertex> getWeightedDistance() {
		return weightedDistance;
	}
}
