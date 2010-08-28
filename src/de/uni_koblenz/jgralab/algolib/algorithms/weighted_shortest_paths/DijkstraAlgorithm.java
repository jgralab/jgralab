package de.uni_koblenz.jgralab.algolib.algorithms.weighted_shortest_paths;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.problems.WeightedDistancesFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.WeightedShortestPathsFromVertexSolver;

public class DijkstraAlgorithm extends AStarSearch implements
		WeightedDistancesFromVertexSolver,
		WeightedShortestPathsFromVertexSolver {

	public DijkstraAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable, DoubleFunction<Edge> edgeWeight) {
		super(graph, subgraph, navigable, edgeWeight, null);
	}

	public DijkstraAlgorithm(Graph graph) {
		this(graph, null, null, null);
	}

	@Override
	public DijkstraAlgorithm normal() {
		super.normal();
		return this;
	}

	@Override
	public DijkstraAlgorithm reversed() {
		super.reversed();
		return this;
	}

	@Override
	public AStarSearch undirected() {
		super.undirected();
		return this;
	}

	@Override
	public DijkstraAlgorithm execute(Vertex start) {
		internalExecute(start, null);
		return this;
	}

	@Override
	public DoubleFunction<Vertex> getWeightedDistance() {
		checkStateForResult();
		return super.getWeightedDistance();
	}
	
	public DoubleFunction<Vertex> getInternalWeightedDistance(){
		return super.getWeightedDistance();
	}

}
