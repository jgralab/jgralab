package de.uni_koblenz.jgralab.algolib.algorithms.topological_order;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitorComposition;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.directed.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.directed.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class TopologicalOrderWithDFS extends GraphAlgorithm implements
		AcyclicitySolver, TopologicalOrderSolver {

	private boolean acyclic;
	private DepthFirstSearch dfs;
	private DFSVisitor acyclicityVisitor;
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
		return acyclic;
	}

	@Override
	public Permutation<Vertex> getTopologicalOrder() {
		checkStateForResult();
		return dfs.getRorder();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		acyclicityVisitor = new DFSVisitorAdapter() {

			@Override
			public void visitBackwardArc(Edge e) {
				acyclic = false;
				throw new AlgorithmTerminatedException();
			}

			@Override
			public void leaveVertex(Vertex v) {
				visitors.visitVertexInTopologicalOrder(v);
			}

		};
		visitors = new TopologicalOrderVisitorComposition();
	}

	@Override
	public void reset() {
		super.reset();
		acyclic = true;
	}

	@Override
	public TopologicalOrderWithDFS execute() {
		dfs.reset();
		EdgeDirection originalDirection = dfs.getSearchDirection();
		dfs.setSearchDirection(EdgeDirection.IN);
		dfs.addVisitor(acyclicityVisitor);
		startRunning();
		try {
			dfs.withRorder().execute();
		} catch (AlgorithmTerminatedException e) {
		}
		done();
		dfs.removeVisitor(acyclicityVisitor);
		dfs.setSearchDirection(originalDirection);
		return this;
	}
}
