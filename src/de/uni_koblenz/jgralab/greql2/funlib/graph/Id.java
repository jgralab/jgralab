package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Id extends Function {

	public Id() {
		super("Returns the id of the given graph element.", Category.GRAPH);
	}

	public Integer evaluate(GraphElement<?, ?> el) {
		return el.getId();
	}
}
