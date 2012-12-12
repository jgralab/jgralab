package de.uni_koblenz.jgralab.greql.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class InverseEdge extends Function {
	@Description(params = "e", description = "Returns the inverse-oriented edge of the given edge e. "
			+ "I.e., if e is a normal (forward-oriented) edge, "
			+ "returns the reversed (backward-oriented) edge and vice versa.", categories = Category.GRAPH)
	public Edge evaluate(Edge e) {
		if (e.isNormal()) {
			return e.getReversedEdge();
		}
		return e.getNormalEdge();
	}
}
