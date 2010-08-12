package de.uni_koblenz.jgralab.algolib.algorithms.acyclicity;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.IterativeDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.directed.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.directed.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class DFSImplementation extends GraphAlgorithm implements
		AcyclicitySolver, TopologicalOrderSolver {

	private boolean acyclic;
	private DepthFirstSearch dfs;
	private DFSVisitor acyclicityVisitor;

	public DFSImplementation(Graph graph, BooleanFunction<GraphElement> subgraph) {
		super(graph, subgraph);
	}

	public DFSImplementation(Graph graph) {
		super(graph);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		throw new UnsupportedOperationException(
				"This algorithm does not support visitors.");
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

		};
	}

	@Override
	public void reset() {
		super.reset();
		acyclic = true;
		dfs = new RecursiveDepthFirstSearch(graph, subgraph, true, null)
				.withRorder();
		dfs.setSearchDirection(EdgeDirection.IN);
		dfs.addVisitor(acyclicityVisitor);
	}

	@Override
	public DFSImplementation execute() {
		startRunning();
		try {
			try {
				dfs.execute();
			} catch (StackOverflowError e) {
				dfs = new IterativeDepthFirstSearch(graph, subgraph, true, null)
						.withRorder();
				dfs.setSearchDirection(EdgeDirection.IN);
				dfs.addVisitor(acyclicityVisitor);
				dfs.execute();
			}
		} catch (AlgorithmTerminatedException e) {
		}
		done();
		return this;
	}
}
