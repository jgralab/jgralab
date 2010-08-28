package de.uni_koblenz.jgralab.algolib.algorithms.strong_components;

import java.util.Stack;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors.ReducedGraphVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors.ReducedGraphVisitorComposition;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.problems.directed.StrongComponentsSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;
import static java.lang.Math.min;

public class StrongComponentsWithDFS extends AbstractTraversal implements
		StrongComponentsSolver {

	private DepthFirstSearch dfs;
	private Stack<Vertex> vertexStack;
	private IntFunction<Vertex> lowlink;
	private Function<Vertex, Vertex> strongComponents;
	private DFSVisitor lowlinkVisitor;
	private ReducedGraphVisitorComposition visitors;

	public StrongComponentsWithDFS(Graph graph, DepthFirstSearch dfs) {
		this(graph, null, dfs, null);
	}

	public StrongComponentsWithDFS(Graph graph,
			BooleanFunction<GraphElement> subgraph, DepthFirstSearch dfs,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
		this.dfs = dfs;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		if (visitor instanceof ReducedGraphVisitor) {
			visitors.addVisitor(visitor);
		} else {
			dfs.addVisitor(visitor);
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
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		if (visitor instanceof ReducedGraphVisitor) {
			visitors.removeVisitor(visitor);
		} else {
			dfs.removeVisitor(visitor);
		}
	}

	@Override
	public Function<Vertex, Vertex> getStrongComponents() {
		checkStateForResult();
		return strongComponents;
	}

	public IntFunction<Vertex> getLowlink() {
		checkStateForResult();
		return lowlink;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		vertexStack = new Stack<Vertex>();
		lowlink = new IntegerVertexMarker(graph);
		strongComponents = new ArrayVertexMarker<Vertex>(graph);
		visitors = new ReducedGraphVisitorComposition();
		lowlinkVisitor = new DFSVisitorAdapter() {

			IntFunction<Vertex> number;

			@Override
			public void setAlgorithm(GraphAlgorithm algorithm) {
				super.setAlgorithm(algorithm);
				number = this.algorithm.getInternalNumber();
			}

			@Override
			public void visitVertex(Vertex v) {
				vertexStack.push(v);
				lowlink.set(v, number.get(v));
			}

			public void maybeVisitReducedEdge(Edge e) {
				if (strongComponents.isDefined(e.getThat())) {
					visitors.visitReducedEdge(e);
				}
			}

			@Override
			public void leaveTreeEdge(Edge e) {
				Vertex v = e.getThis();
				Vertex w = e.getThat();
				lowlink.set(v, min(lowlink.get(v), lowlink.get(w)));
				maybeVisitReducedEdge(e);
			}

			@Override
			public void visitForwardArc(Edge e) {
				maybeVisitReducedEdge(e);
			}

			@Override
			public void visitBackwardArc(Edge e) {
				Vertex v = e.getThis();
				Vertex w = e.getThat();
				lowlink.set(v, min(lowlink.get(v), number.get(w)));
			}

			@Override
			public void visitCrosslink(Edge e) {
				Vertex v = e.getThis();
				Vertex w = e.getThat();
				if (vertexStack.contains(w)) {
					lowlink.set(v, min(lowlink.get(v), number.get(w)));
				}
				maybeVisitReducedEdge(e);
			}

			@Override
			public void leaveVertex(Vertex v) {
				if (lowlink.get(v) == number.get(v)) {
					Vertex x;
					do {
						x = vertexStack.pop();
						strongComponents.set(x, v);
						// visit the representative vertex
						// TODO visit reduced edges
						visitors.visitRepresentativeVertex(v);
					} while (x != v);
				}
			}

		};
	}

	@Override
	public void reset() {
		super.reset();
		vertexStack.clear();
	}

	@Override
	public StrongComponentsSolver execute() {
		dfs.reset();
		dfs.setGraph(graph);
		dfs.setSubgraph(subgraph);
		dfs.setNavigable(navigable);
		dfs.setSearchDirection(searchDirection);
		dfs.addVisitor(lowlinkVisitor);
		startRunning();
		dfs.execute();
		done();
		dfs.removeVisitor(lowlinkVisitor);
		return this;
	}

}
