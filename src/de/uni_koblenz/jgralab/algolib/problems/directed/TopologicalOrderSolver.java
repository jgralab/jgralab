package de.uni_koblenz.jgralab.algolib.problems.directed;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.IntDomainFunction;

//TODO write problem specification
public interface TopologicalOrderSolver {
	
	public TopologicalOrderSolver execute();

	public IntDomainFunction<Vertex> getTopologicalOrder();
}
