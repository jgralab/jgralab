package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.ComputeNumberVisitor;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitorComposition;
import de.uni_koblenz.jgralab.algolib.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.visitors.SimpleVisitor;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public abstract class DepthFirstSearch extends SearchAlgorithm {

	protected DFSVisitorComposition visitors;
	protected ComputeNumberVisitor cnv;
	protected IntFunction<Vertex> number;
	protected int rNum;
	protected IntFunction<Vertex> rnumber;

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
		number = cnv.getIntermediateNumber();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new DFSVisitorComposition();
		cnv = new ComputeNumberVisitor();
		cnv.setAlgorithm(this);
		visitors.addSearchVisitor(cnv);
	
	}

	@Override
	public void addSearchVisitor(SearchVisitor visitor) {
		visitor.setAlgorithm(this);
		visitors.addSearchVisitor(visitor);
	}

	@Override
	public void addSimpleVisitor(SimpleVisitor visitor) {
		visitor.setAlgorithm(this);
		visitors.addSimpleVisitor(visitor);
	}

	public void addDFSVisitor(DFSVisitor visitor) {
		visitor.setAlgorithm(this);
		visitors.addDFSVisitor(visitor);
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

	public IntFunction<Vertex> getNumber() {
		if (state == AlgorithmStates.FINISHED
				|| state == AlgorithmStates.STOPPED) {
			return number;
		} else {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ state);
		}
	}

	public IntFunction<Vertex> getRnumber() {
		if (state == AlgorithmStates.FINISHED
				|| state == AlgorithmStates.STOPPED) {
			return rnumber;
		} else {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ state);
		}
	}

}