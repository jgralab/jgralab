package de.uni_koblenz.jgralab.greql.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;

public class IncidenceIndex extends Function {

	@Description(params = { "e", "v" }, description = "Returns the index of e in the incidence sequence of v.\n"
			+ "Returns -1 if e is not in v's incidence sequence.", categories = Category.GRAPH)
	public Integer evaluate(Edge e, Vertex v) {
		return eval(e, v);
	}

	protected static Integer eval(Edge e, Vertex v) {
		int i = 0;
		for (Edge inc : v.incidences()) {
			if (inc == e) {
				return i;
			}
			i++;
		}
		return -1;
	}
}
