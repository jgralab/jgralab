package de.uni_koblenz.jgralab.greql.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql.funlib.Description;

public class OmegaIncidenceIndex extends IncidenceIndex {
	@Description(params = { "e" }, description = "Returns the index of e in the incidence sequence of its omega vertex.\n", categories = Category.GRAPH)
	public Integer evaluate(Edge e) {
		return eval(e, e.getOmega());
	}
}
