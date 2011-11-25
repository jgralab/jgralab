package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Omega extends Function {
	public Omega() {
		super("Returns the end vertex of an edge.", 1, 1, 1.0, Category.GRAPH);
	}

	public Vertex evaluate(Edge e) {
		return e.getOmega();
	}
}