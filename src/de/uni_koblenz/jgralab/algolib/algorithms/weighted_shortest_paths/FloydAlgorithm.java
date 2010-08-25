package de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.reachability.visitors.TransitiveVisitorComposition;
import de.uni_koblenz.jgralab.algolib.algorithms.search.AbstractTraversal;
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
import de.uni_koblenz.jgralab.algolib.problems.WeightedDistancesSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedShortestPathsSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class FloydAlgorithm extends AbstractTraversal implements
		WeightedDistancesSolver, WeightedShortestPathsSolver,
		NegativeCyclesSolver {

	private TransitiveVisitorComposition visitors;
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
		checkStateForSettingParameters();
		visitors.addVisitor(visitor);
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
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		visitors.removeVisitor(visitor);
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
		SearchAlgorithm search = new BreadthFirstSearch(graph)
				.withNumber();
		search.execute();
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
		searchDirection = EdgeDirection.OUT;
		visitors = new TransitiveVisitorComposition();
	}

	@Override
	public FloydAlgorithm execute() {
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
			switch (searchDirection) {
			case OUT:
				weightedDistance[vId][wId] = edgeWeight.get(e);
				successor[vId][wId] = e;
				break;
			case INOUT:
				weightedDistance[vId][wId] = edgeWeight.get(e);
				weightedDistance[wId][vId] = edgeWeight.get(e);
				successor[vId][wId] = e;
				successor[wId][vId] = e.getReversedEdge();
				break;
			case IN:
				weightedDistance[wId][vId] = edgeWeight.get(e);
				successor[wId][vId] = e.getReversedEdge();
			}
		}

		// main loop
		for (int vId = 1; vId <= vertexCount; vId++) {
			for (int uId = 1; uId <= vertexCount; uId++) {
				for (int wId = 1; wId <= vertexCount; wId++) {
					double newDistance = weightedDistance[uId][vId]
							+ weightedDistance[vId][wId];
					if (weightedDistance[uId][wId] > newDistance) {
						weightedDistance[uId][wId] = newDistance;
						successor[uId][wId] = successor[uId][vId];
						visitors.visitVertexTriple(vertexOrder.get(uId),
								vertexOrder.get(vId), vertexOrder.get(wId));
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
	public BinaryDoubleFunction<Vertex, Vertex> getWeightedDistance() {
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

}
