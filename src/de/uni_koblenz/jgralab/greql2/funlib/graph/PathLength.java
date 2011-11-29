package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Path;

public class PathLength extends Function {

	public PathLength() {
		super("Returns the length of the given Path.", Category.GRAPH);
	}

	public Integer evaluate(Path p) {
		return p.getLength();
	}
}
