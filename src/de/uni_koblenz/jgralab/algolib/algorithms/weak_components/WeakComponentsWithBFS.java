package de.uni_koblenz.jgralab.algolib.algorithms.weak_components;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.problems.WeakComponentsSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;

public class WeakComponentsWithBFS extends StructureOrientedAlgorithm implements
		WeakComponentsSolver {

	private BreadthFirstSearch bfs;
	private SearchVisitor weakComponentsVisitor;

	private Function<Vertex, Vertex> weakComponents;
	private int kappa;

	public WeakComponentsWithBFS(Graph graph, BreadthFirstSearch bfs) {
		this(graph, null, bfs, null);
	}

	public WeakComponentsWithBFS(Graph graph,
			BooleanFunction<GraphElement> subgraph, BreadthFirstSearch bfs,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
		this.bfs = bfs;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		// the algorithm is set implicitly to the bfs
		bfs.addVisitor(visitor);
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		bfs.removeVisitor(visitor);
	}

	@Override
	public WeakComponentsWithBFS normal() {
		throw new UnsupportedOperationException(
				"This algorithm is only defined for undirected graphs.");
	}

	@Override
	public WeakComponentsWithBFS reversed() {
		throw new UnsupportedOperationException(
				"This algorithm is only defined for undirected graphs.");
	}

	@Override
	public WeakComponentsWithBFS undirected() {
		super.undirected();
		return this;
	}

	@Override
	public boolean isHybrid() {
		return false;
	}

	@Override
	public void reset() {
		super.reset();
		weakComponents = new ArrayVertexMarker<Vertex>(graph);
		kappa = 0;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		this.navigable = null;
		this.traversalDirection = EdgeDirection.INOUT;
		weakComponentsVisitor = new SearchVisitorAdapter() {

			private Vertex currentRepresentativeVertex;

			@Override
			public void visitRoot(Vertex v) throws AlgorithmTerminatedException {
				kappa++;
				currentRepresentativeVertex = v;
			}

			@Override
			public void visitVertex(Vertex v)
					throws AlgorithmTerminatedException {
				weakComponents.set(v, currentRepresentativeVertex);
			}

		};
	}

	@Override
	public void disableOptionalResults() {
	}

	@Override
	public WeakComponentsWithBFS execute() throws AlgorithmTerminatedException {
		bfs.reset();
		bfs.setGraph(graph);
		bfs.setSubgraph(subgraph);
		bfs.setNavigable(navigable);
		bfs.undirected();
		bfs.addVisitor(weakComponentsVisitor);
		startRunning();
		bfs.execute();
		bfs.removeVisitor(weakComponentsVisitor);
		done();
		return this;
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public Function<Vertex, Vertex> getWeakComponents() {
		checkStateForResult();
		return weakComponents;
	}

	@Override
	public int getKappa() {
		checkStateForResult();
		return kappa;
	}

	public Function<Vertex, Vertex> getInternalWeakComponents() {
		return weakComponents;
	}

	public int getInternalKappa() {
		return kappa;
	}

}
