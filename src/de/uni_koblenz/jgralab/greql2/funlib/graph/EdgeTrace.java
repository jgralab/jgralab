package de.uni_koblenz.jgralab.greql2.funlib.graph;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Path;

public class EdgeTrace extends Function {

	public EdgeTrace() {
		super("Returns the edge trace of a Path $p$.",
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES);
	}

	public PVector<Edge> evaluate(Path p) {
		return p.getEdgeTrace();
	}
}
