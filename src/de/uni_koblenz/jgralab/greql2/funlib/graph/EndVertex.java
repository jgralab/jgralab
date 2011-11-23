package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Path;

public class EndVertex extends Function {

	public EndVertex() {
		super("Returns the end vertex of the given edge or path.",
				Category.GRAPH, Category.PATHS_AND_PATHSYSTEMS_AND_SLICES);
	}

	public Vertex evaluate(Edge e) {
		return e.getOmega();
	}

	public Vertex evaluate(Path p) {
		return p.getEndVertex();
	}

}
