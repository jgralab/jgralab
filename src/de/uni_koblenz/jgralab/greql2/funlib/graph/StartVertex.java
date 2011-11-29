package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Path;

public class StartVertex extends Function {

	public StartVertex() {
		super("Returns the start vertex of an edge or a path.", Category.GRAPH,
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES);
	}

	public Vertex evaluate(Edge e) {
		return e.getAlpha();
	}

	public Vertex evaluate(Path p) {
		return p.getStartVertex();
	}
}
