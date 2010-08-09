package de.uni_koblenz.jgralab.algolib.problems.directed;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;

//TODO write problem specification
public interface StrongComponentsSolver {

	public StrongComponentsSolver execute();

	public Function<Vertex, Vertex> getStrongComponents();
}
