package de.uni_koblenz.jgralab.algolib.algorithms.topological_order;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitorComposition;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.directed.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.directed.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class TopologicalOrderWithDFS extends AbstractTraversal implements
		AcyclicitySolver, TopologicalOrderSolver {

	private DepthFirstSearch dfs;
	private boolean acyclic;
	private DFSVisitorAdapter torderVisitorAdapter;
	private TopologicalOrderVisitorComposition visitors;

	public TopologicalOrderWithDFS(Graph graph,
			BooleanFunction<GraphElement> subgraph, DepthFirstSearch dfs,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
		this.dfs = dfs;
	}

	public TopologicalOrderWithDFS(Graph graph, DepthFirstSearch dfs) {
		this(graph, null, dfs, null);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		if (visitor instanceof TopologicalOrderVisitor) {
			visitor.setAlgorithm(this);
			visitors.addVisitor(visitor);
		} else {
			// the algorithm is set implicitly to the dfs
			dfs.addVisitor(visitor);
		}
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
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
		state = dfs.getState();
	}

	@Override
	public AbstractTraversal normal() {
		super.normal();
		dfs.reversed();
		return this;
	}

	@Override
	public AbstractTraversal reversed() {
		super.reversed();
		dfs.normal();
		return this;
	}

	@Override
	public boolean isHybrid() {
		return false;
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
		visitors = new TopologicalOrderVisitorComposition();
		torderVisitorAdapter = new DFSVisitorAdapter() {
			@Override
			public void visitBackwardArc(Edge e) {
				acyclic = false;
				dfs.terminate();
			}

			@Override
			public void leaveVertex(Vertex v) {
				visitors.visitVertexInTopologicalOrder(v);
			}
		};
		assert (DEFAULT_SEARCH_DIRECTION == EdgeDirection.OUT);
		dfs.reversed();
	}

	@Override
	public void reset() {
		super.reset();
	}

	@Override
	public TopologicalOrderWithDFS execute() {
		dfs.reset();
		dfs.setGraph(graph);
		dfs.setSubgraph(subgraph);
		dfs.setNavigable(navigable);
		dfs.addVisitor(torderVisitorAdapter);
		startRunning();
		try {
			dfs.withRorder().execute();
		} catch (AlgorithmTerminatedException e) {
		}
		done();
		dfs.removeVisitor(torderVisitorAdapter);
		return this;
	}
}
