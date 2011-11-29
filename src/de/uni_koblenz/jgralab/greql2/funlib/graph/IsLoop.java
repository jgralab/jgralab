package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class IsLoop extends Function {

	public IsLoop() {
		super(
				"Returns true, iff the given edge is a loop, i.e. it starts and ends at the same vertex.",
				1, 1, 0.01, Category.GRAPH);
	}

	public Boolean evaluate(Edge e) {
		return e.getAlpha() == e.getOmega();
	}
}
