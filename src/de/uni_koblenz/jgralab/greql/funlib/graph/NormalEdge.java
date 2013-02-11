package de.uni_koblenz.jgralab.greql.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class NormalEdge extends Function {

	@Description(params = "e", description = "Returns the forward-oriented edge of the given edge e. "
			+ "If e is already forward-oriented simply returns e.", categories = Category.GRAPH)
	public Edge evaluate(Edge e) {
		return e.getNormalEdge();
	}
}
