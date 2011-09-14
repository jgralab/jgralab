package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
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

	public Boolean evaluate(SubGraphMarker subgraph, Vertex v) {
		if (!subgraph.isMarked(v)) {
			return null;
		}
		int degree = 0;
		for (Edge e : v.incidences()) {
			if (subgraph.isMarked(e)) {
				++degree;
			}
		}
		return degree == 0;
	}
}
