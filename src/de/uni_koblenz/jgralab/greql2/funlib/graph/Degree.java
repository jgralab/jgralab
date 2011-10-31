package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.EdgeDirection;

public class Degree extends DegreeFunction {

	public Degree() {
		super("Returns the degree of the given vertex.\n"
				+ "The scope can be limited by a path, a path system, or\n"
				+ "an type collection.", EdgeDirection.INOUT);
	}

}
