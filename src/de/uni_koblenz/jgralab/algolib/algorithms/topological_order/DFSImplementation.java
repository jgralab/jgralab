package de.uni_koblenz.jgralab.algolib.algorithms.topological_order;

import java.util.Map;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.directed.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.directed.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.visitors.SimpleVisitor;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class DFSImplementation extends GraphAlgorithm implements
		AcyclicitySolver, TopologicalOrderSolver {

	private boolean acyclic;
	private DepthFirstSearch dfs;
	private DFSVisitor acyclicityVisitor;
	private Map<Visitor, Visitor> visitors;

	public DFSImplementation(Graph graph,
			BooleanFunction<GraphElement> subgraph, DepthFirstSearch dfs) {
		super(graph, subgraph);
		this.dfs = dfs;
	}

	public DFSImplementation(Graph graph, DepthFirstSearch dfs) {
		this(graph, null, dfs);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		if (visitor instanceof SimpleVisitor) {
			final SimpleVisitor actualVisitor = (SimpleVisitor) visitor;
			DFSVisitorAdapter adapter = new DFSVisitorAdapter() {

				@Override
				public void leaveVertex(Vertex v) {
					actualVisitor.visitVertex(v);
				}

				@Override
				public void visitEdge(Edge e) {
					actualVisitor.visitEdge(e);
				}

			};
			visitors.put(visitor, adapter);
			dfs.addVisitor(adapter);
		} else {
			throw new IllegalArgumentException(
					"The given visitor is incompatible with this algorithm.");
		}
	}
	
	@Override
	public void removeVisitor(Visitor visitor){
		checkStateForSettingParameters();
		Visitor toDelete = visitors.remove(visitor);
		dfs.removeVisitor(toDelete);
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
		visitors = null;
	}

	@Override
	public void reset() {
		super.reset();
		acyclic = true;
	}

	@Override
	public DFSImplementation execute() {
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
		dfs.withoutRorder();
		dfs.removeVisitor(acyclicityVisitor);
		dfs.setSearchDirection(originalDirection);
		return this;
	}
}
