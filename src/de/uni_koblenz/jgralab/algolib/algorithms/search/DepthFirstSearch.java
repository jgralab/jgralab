package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.ArrayFunction;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntDomainFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitorComposition;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public abstract class DepthFirstSearch extends SearchAlgorithm {

	protected DFSVisitorComposition visitors;
	protected int rNum;
	protected IntFunction<Vertex> rnumber;
	protected Vertex[] rorder;

	public DepthFirstSearch(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, directed, navigable);
	}

	public DepthFirstSearch(Graph graph) {
		super(graph);
	}

	@Override
	public void reset() {
		super.reset();
		visitors.reset();
		rNum = 1;
		rnumber = new IntegerVertexMarker(graph);
		number = new IntegerVertexMarker(graph);
	}

	@Override
	public DepthFirstSearch withLevel() {
		return (DepthFirstSearch) super.withLevel();
	}

	@Override
	public DepthFirstSearch withNumber() {
		throw new UnsupportedOperationException(
				"The result \"number\" is mandatory for DFS and doesn't need to be explicitly activated.");
	}

	@Override
	public DepthFirstSearch withParent() {
		return (DepthFirstSearch) super.withParent();
	}

	public DepthFirstSearch withRorder() {
		checkStateForSettingParameters();
		rorder = new Vertex[graph.getVCount() + 1];
		return this;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new DFSVisitorComposition();
	}

	@Override
	public void addVisitor(Visitor visitor) {
		visitor.setAlgorithm(this);
		visitors.addVisitor(visitor);
	}

	public Vertex[] getInternalRorder() {
		return rorder;
	}

	public int getIntermediateRNum() {
		return rNum;
	}

	public IntFunction<Vertex> getIntermediateRnumber() {
		return rnumber;
	}

	public IntFunction<Vertex> getIntermediateNumber() {
		return number;
	}

	public IntFunction<Vertex> getRnumber() {
		checkStateForResult();
		return rnumber;
	}

	public IntDomainFunction<Vertex> getRorder() {
		checkStateForResult();
		return rorder == null ? null : new ArrayFunction<Vertex>(rorder);
	}

}