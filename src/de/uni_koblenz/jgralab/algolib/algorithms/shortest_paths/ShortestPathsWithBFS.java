package de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.problems.DistanceFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.ShortestPathsFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class ShortestPathsWithBFS extends GraphAlgorithm implements
		DistanceFromVertexSolver, ShortestPathsFromVertexSolver {

	private BreadthFirstSearch bfs;

	public ShortestPathsWithBFS(Graph graph, BreadthFirstSearch bfs) {
		this(graph, null, bfs);
	}

	public ShortestPathsWithBFS(Graph graph,
			BooleanFunction<GraphElement> subgraph, BreadthFirstSearch bfs) {
		super(graph, subgraph);
		this.bfs = bfs;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		bfs.addVisitor(visitor);
	}

	@Override
	public void disableOptionalResults() {
		bfs.disableOptionalResults();
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public boolean isDirected() {
		return bfs.isDirected();
	}

	@Override
	public boolean isHybrid() {
		return bfs.isHybrid();
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		bfs.removeVisitor(visitor);
	}

	@Override
	public void setDirected(boolean directed) {
		bfs.setDirected(directed);
	}

	@Override
	public ShortestPathsWithBFS execute(Vertex start) {
		bfs.reset();
		startRunning();
		bfs.withLevel().withParent().execute(start);
		done();
		return this;
	}

	@Override
	public IntFunction<Vertex> getDistance() {
		checkStateForResult();
		return bfs.getLevel();
	}

	@Override
	public Function<Vertex, Edge> getParent() {
		return bfs.getParent();
	}

}
