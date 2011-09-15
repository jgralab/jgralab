package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public abstract class IsIsolated extends Function {

	public IsIsolated() {
		super(
				"Returns true iff the vertex $v$ has no incidences (possibly restricted to subgraph).",
				10, 1, 1, Category.GRAPH);
	}

	public Boolean evaluate(Vertex v) {
		return v.getDegree() == 0;
	}
}
