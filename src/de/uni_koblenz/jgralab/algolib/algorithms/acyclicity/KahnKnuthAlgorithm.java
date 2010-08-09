package de.uni_koblenz.jgralab.algolib.algorithms.acyclicity;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntDomainFunction;
import de.uni_koblenz.jgralab.algolib.problems.directed.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.directed.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class KahnKnuthAlgorithm extends GraphAlgorithm implements
		AcyclicitySolver, TopologicalOrderSolver {

	public KahnKnuthAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph) {
		super(graph, subgraph);
		// TODO Auto-generated constructor stub
	}

	public KahnKnuthAlgorithm(Graph graph) {
		super(graph);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addVisitor(Visitor visitor) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void done() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDirected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public KahnKnuthAlgorithm execute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAcyclic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IntDomainFunction<Vertex> getTopologicalOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disableOptionalResults() {
		// TODO Auto-generated method stub
		
	}

}
