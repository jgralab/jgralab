package de.uni_koblenz.jgralab.algolib.algorithms.topological_order;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitorComposition;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitorForDFS;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.directed.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.directed.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class TopologicalOrderWithDFS extends GraphAlgorithm implements
		AcyclicitySolver, TopologicalOrderSolver {

	private DepthFirstSearch dfs;
	private TopologicalOrderVisitorForDFS torderVisitorAdapter;
	private TopologicalOrderVisitorComposition visitors;

	public TopologicalOrderWithDFS(Graph graph,
			BooleanFunction<GraphElement> subgraph, DepthFirstSearch dfs) {
		super(graph, subgraph);
		this.dfs = dfs;
	}

	public TopologicalOrderWithDFS(Graph graph, DepthFirstSearch dfs) {
		this(graph, null, dfs);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		if (visitor instanceof TopologicalOrderVisitor) {
			visitors.addVisitor(visitor);
		} else {
			dfs.addVisitor(visitor);
		}
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		if (visitor instanceof TopologicalOrderVisitor) {
			visitors.removeVisitor(visitor);
		} else {
			dfs.removeVisitor(visitor);
		}
	}

	@Override
	public void disableOptionalResults() {

	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public boolean isHybrid() {
		return false;
	}

	@Override
	public void setDirected(boolean directed) {
		throw new UnsupportedOperationException(
				"This algorithm only works for directed graphs.");
	}

	@Override
	public boolean isAcyclic() {
		checkStateForResult();
		return torderVisitorAdapter.isAcyclic();
	}

	@Override
	public Permutation<Vertex> getTopologicalOrder() {
		checkStateForResult();
		return dfs.getRorder();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new TopologicalOrderVisitorComposition();
		torderVisitorAdapter = new TopologicalOrderVisitorForDFS(visitors);
	}

	@Override
	public void reset() {
		super.reset();
	}

	@Override
	public TopologicalOrderWithDFS execute() {
		dfs.reset();
		EdgeDirection originalDirection = dfs.getSearchDirection();
		dfs.setSearchDirection(EdgeDirection.IN);
		dfs.addVisitor(torderVisitorAdapter);
		startRunning();
		try {
			dfs.withRorder().execute();
		} catch (AlgorithmTerminatedException e) {
		}
		done();
		dfs.removeVisitor(torderVisitorAdapter);
		dfs.setSearchDirection(originalDirection);
		return this;
	}
}
