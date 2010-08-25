package de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths;

import java.util.LinkedList;
import java.util.Queue;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.search.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.problems.TraversalSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedDistanceFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedDistancesFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedShortestPathFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedShortestPathsFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.DoubleVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class FordMooreAlgorithm extends AbstractTraversal implements
		TraversalSolver, WeightedDistancesFromVertexSolver,
		WeightedShortestPathsFromVertexSolver,
		WeightedDistanceFromVertexToVertexSolver,
		WeightedShortestPathFromVertexToVertexSolver {

	private DoubleFunction<Edge> edgeWeight;
	private Vertex target;

	private Function<Vertex, Edge> parent;
	private DoubleFunction<Vertex> weightedDistance;

	private Queue<Vertex> vertexQueue;
	private IntFunction<Vertex> pushCount;
	private int maxPushCount;
	private boolean negativeCycleDetected;

	public FordMooreAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable, DoubleFunction<Edge> weight) {
		super(graph, subgraph,navigable);
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
	public boolean isHybrid() {
		return true;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		throw new UnsupportedOperationException(
				"This algorithm currently doesn't support visitors!");
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		throw new UnsupportedOperationException(
				"This algorithm currently doesn't support visitors!");
	}

	@Override
	public void reset() {
		super.reset();
		parent = new ArrayVertexMarker<Edge>(graph);
		weightedDistance = new DoubleVertexMarker(graph);
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
		searchDirection = EdgeDirection.OUT;
	}

	@Override
	public FordMooreAlgorithm execute(Vertex start) {
		if (subgraph != null && !subgraph.get(start)) {
			throw new IllegalArgumentException("Start vertex not in subgraph!");
		}
		startRunning();
		for (Vertex currentVertex : graph.vertices()) {
			if (subgraph == null || subgraph.get(currentVertex)) {
				pushCount.set(currentVertex, 0);
			}
		}
		weightedDistance.set(start, 0.0);
		vertexQueue.add(start);
		pushCount.set(start, pushCount.get(start) + 1);

		while (!vertexQueue.isEmpty()) {
			Vertex currentVertex = vertexQueue.poll();
			assert (currentVertex != null);
			for (Edge currentEdge : currentVertex.incidences(searchDirection)) {
				if (subgraph != null && !subgraph.get(currentEdge)
						|| navigable != null && !navigable.get(currentEdge)) {
					continue;
				}
				Vertex nextVertex = currentEdge.getThat();
				assert (subgraph.get(nextVertex));
				double newDistance = weightedDistance.get(currentVertex)
						+ (edgeWeight == null ? 1.0 : edgeWeight
								.get(currentEdge));
				if (!weightedDistance.isDefined(nextVertex)
						|| newDistance < weightedDistance.get(nextVertex)) {
					parent.set(nextVertex, currentEdge);
					weightedDistance.set(nextVertex, newDistance);
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
	public FordMooreAlgorithm execute(Vertex start, Vertex target) {
		if (subgraph != null && !subgraph.get(target)) {
			throw new IllegalArgumentException("Target vertex not in subgraph!");
		}
		this.target = target;
		return execute(start);
	}

	@Override
	public DoubleFunction<Vertex> getWeightedDistance() {
		checkStateForResult();
		return weightedDistance;
	}

	@Override
	public double getSingleWeightedDistance() {
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

	public boolean hasNegativeCycleDetected() {
		return negativeCycleDetected;
	}

}
