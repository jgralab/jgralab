package de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorComposition;
import de.uni_koblenz.jgralab.algolib.buffers.PriorityQueue;
import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.problems.WeightedDistanceFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedShortestPathFromVertexToVertexSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.DoubleVertexMarker;

public class AStarSearch extends SearchAlgorithm implements
		WeightedDistanceFromVertexToVertexSolver,
		WeightedShortestPathFromVertexToVertexSolver {

	protected DoubleFunction<Vertex> weightedDistance;

	protected DoubleFunction<Edge> edgeWeight;
	private BinaryDoubleFunction<Vertex, Vertex> heuristic;
	protected Vertex target;
	protected SearchVisitorAdapter targetVertexReachedVisitor;

	protected PriorityQueue<Vertex> vertexQueue;
	protected SearchVisitorComposition visitors;

	public AStarSearch(Graph graph, BooleanFunction<GraphElement> subgraph,
			boolean directed, BooleanFunction<Edge> navigable,
			DoubleFunction<Edge> edgeWeight,
			BinaryDoubleFunction<Vertex, Vertex> heuristic) {
		super(graph, subgraph, directed, navigable);
		this.edgeWeight = edgeWeight;
		this.heuristic = heuristic;
	}

	public AStarSearch(Graph graph) {
		this(graph, null, true, null, null, null);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		visitors.addVisitor(visitor);
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		visitors.removeVisitor(visitor);
	}

	@Override
	public AStarSearch withLevel() {
		return (AStarSearch) super.withLevel();
	}

	@Override
	public AStarSearch withNumber() {
		return (AStarSearch) super.withNumber();
	}

	@Override
	public AStarSearch withParent() {
		throw new UnsupportedOperationException(
				"The result \"parent\" is mandatory for A* and doesn't need to be explicitly activated.");

	}

	@Override
	public AStarSearch withoutLevel() {
		return (AStarSearch) super.withoutLevel();
	}

	@Override
	public AStarSearch withoutNumber() {
		return (AStarSearch) super.withoutNumber();
	}

	@Override
	public AStarSearch withoutParent() {
		throw new UnsupportedOperationException(
				"The result \"parent\" is mandatory for A* and cannot be deactivated.");
	}

	@Override
	public void reset() {
		super.reset();
		visitors.reset();
		weightedDistance = new DoubleVertexMarker(graph);
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
		try {
			execute(start);
		} catch (AlgorithmTerminatedException e) {
		}
		visitors.removeVisitor(targetVertexReachedVisitor);
		return this;
	}

	@Override
	public AStarSearch execute(Vertex start) {
		if (subgraph != null && !subgraph.get(start)) {
			throw new IllegalArgumentException("Start vertex not in subgraph!");
		}
		startRunning();
		weightedDistance.set(start, 0);
		if (level != null) {
			level.set(start, 0);
		}
		visitors.visitRoot(start);
		vertexQueue.put(start, 0);

		// main loop
		while (!vertexQueue.isEmpty()) {
			Vertex currentVertex = vertexQueue.getNext();
			if (!visitedVertices.get(currentVertex)) {
				vertexOrder[num] = currentVertex;
				if (level != null) {
					level.set(currentVertex, level.get(parent
							.get(currentVertex).getThis()) + 1);
				}
				if (number != null) {
					number.set(currentVertex, num);
				}
				visitors.visitVertex(currentVertex);
				visitedVertices.set(currentVertex, true);
				num++;
				for (Edge currentEdge : currentVertex
						.incidences(searchDirection)) {
					if (subgraph != null && subgraph.get(currentEdge)
							|| navigable != null && navigable.get(currentEdge)) {
						continue;
					}
					Vertex nextVertex = currentEdge.getThat();
					assert (subgraph == null || subgraph.get(nextVertex));
					double newDistance = weightedDistance.get(currentVertex)
							+ (edgeWeight == null ? 1.0 : edgeWeight
									.get(currentEdge));
					edgeOrder[eNum] = currentEdge;
					visitors.visitEdge(currentEdge);
					visitedEdges.set(currentEdge, true);
					eNum++;
					if (!visitedVertices.isDefined(nextVertex)) {
						visitors.visitTreeEdge(currentEdge);
					} else {
						visitors.visitFrond(currentEdge);
					}

					if (!weightedDistance.isDefined(nextVertex)
							|| weightedDistance.get(nextVertex) > newDistance) {
						parent.set(nextVertex, currentEdge);
						weightedDistance.set(nextVertex, newDistance);
						vertexQueue.put(nextVertex, newDistance
								+ (heuristic == null ? 0 : heuristic.get(
								// TODO das gefällt mir nicht!!!
										nextVertex, target)));
					}
				}
			}
		}

		done();
		return this;
	}

	public void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public double getSingleWeightedDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

}
