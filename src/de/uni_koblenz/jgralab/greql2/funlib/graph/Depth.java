package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.PathSystem;

public class Depth extends Function {

	public Depth() {
		super("Returns the depth of the given path system.",
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES);
	}

	public Integer evaluate(PathSystem p) {
		return p.getDepth();
	}

}
