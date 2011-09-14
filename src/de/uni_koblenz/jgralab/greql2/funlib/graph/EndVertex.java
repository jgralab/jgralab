package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Path;

public class EndVertex extends Function {

	public EndVertex() {
		super("Returns $\\omega(e)$, or the end vertex of a Path $p$.",
				Category.GRAPH);
	}

	public Vertex evaluate(Edge e) {
		return e.getOmega();
	}

	public Vertex evaluate(Path p) {
		return p.getEndVertex();
	}

}
