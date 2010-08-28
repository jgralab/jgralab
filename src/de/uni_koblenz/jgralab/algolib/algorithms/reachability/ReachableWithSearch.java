package de.uni_koblenz.jgralab.algolib.algorithms.reachability;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.ReachableSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class ReachableWithSearch extends AbstractTraversal implements
		ReachableSolver {

	private SearchAlgorithm search;
	private SearchVisitor reachableVisitor;
	private boolean reachable;
	private Vertex target;

	public ReachableWithSearch(Graph graph,
			BooleanFunction<GraphElement> subgraph, SearchAlgorithm search,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
		this.search = search;
	}

	public ReachableWithSearch(Graph graph, SearchAlgorithm search) {
		this(graph, null, search, null);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		search.addVisitor(visitor);
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		search.removeVisitor(visitor);
	}

	@Override
	public void disableOptionalResults() {
		search.disableOptionalResults();
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public boolean isDirected() {
		return search.isDirected();
	}

	@Override
	public boolean isHybrid() {
		return search.isHybrid();
	}

	@Override
	public void reset() {
		super.reset();
		target = null;
		reachable = false;
	}

	@Override
	public void resetParameters() {
		reachableVisitor = new SearchVisitorAdapter() {

			@Override
			public void visitVertex(Vertex v) {
				if (v == target) {
					reachable = true;
					search.terminate();
				}
			}

		};
	}

	@Override
	public ReachableSolver execute(Vertex start, Vertex target) {
		search.reset();
		search.setGraph(graph);
		search.setSubgraph(subgraph);
		search.setNavigable(navigable);
		search.setSearchDirection(searchDirection);
		search.addVisitor(reachableVisitor);
		startRunning();
		this.target = target;
		try {
			search.execute(start);
		} catch (AlgorithmTerminatedException e) {
			System.out.println("early termination");
		}
		done();
		search.removeVisitor(reachableVisitor);
		return this;
	}

	@Override
	public boolean isReachable() {
		checkStateForResult();
		return reachable;
	}

}
