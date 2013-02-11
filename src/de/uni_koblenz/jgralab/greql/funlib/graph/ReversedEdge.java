package de.uni_koblenz.jgralab.greql.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class ReversedEdge extends Function {

	@Description(params = "e", description = "Returns the backward-oriented edge of the given edge e. "
			+ "If e is already backward-oriented simply returns e.", categories = Category.GRAPH)
	public Edge evaluate(Edge e) {
		return e.getReversedEdge();
	}
}
