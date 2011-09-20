package de.uni_koblenz.jgralab.algolib.algorithms.weak_components;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.IsTreeSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class IsTree extends StructureOrientedAlgorithm implements IsTreeSolver {

	private WeakComponentsWithBFS wcbfs;
	private SearchVisitor isTreeVisitor;
	private boolean isTree;

	public IsTree(Graph graph, BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
	}

	public IsTree(Graph graph) {
		this(graph, null, null);
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		traversalDirection = EdgeDirection.INOUT;
		wcbfs = new WeakComponentsWithBFS(graph, new BreadthFirstSearch(graph));
		isTreeVisitor = new SearchVisitorAdapter() {

			@Override
			public void visitFrond(Edge e) throws AlgorithmTerminatedException {
				terminate();
			}

		};
	}

	@Override
	public void reset() {
		super.reset();
		isTree = false;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		wcbfs.addVisitor(visitor);
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
		return false;
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		wcbfs.removeVisitor(visitor);
	}

	@Override
	public IsTreeSolver execute() throws AlgorithmTerminatedException {
		wcbfs.reset();
		wcbfs.setGraph(graph);
		wcbfs.setSubgraph(subgraph);
		wcbfs.setNavigable(navigable);
		wcbfs.addVisitor(isTreeVisitor);
		try {
			startRunning();
			wcbfs.execute();
			isTree = wcbfs.getKappa() <= 1;
		} catch (AlgorithmTerminatedException e) {
			isTree = false;
		}
		wcbfs.removeVisitor(isTreeVisitor);
		done();
		return this;
	}

	@Override
	public boolean isTree() throws IllegalStateException {
		checkStateForResult();
		return isTree;
	}

	public boolean getInternalIsTree() {
		return isTree;
	}

}
