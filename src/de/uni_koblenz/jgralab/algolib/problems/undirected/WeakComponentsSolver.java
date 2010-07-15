package de.uni_koblenz.jgralab.algolib.problems.undirected;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;


public interface WeakComponentsSolver {
	public void solveWeakComponents();

	public Function<Vertex, Vertex> getWeakComponents();
}
