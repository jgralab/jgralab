package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.funlib.NeedsGraphArgument;

@NeedsGraphArgument
public class GetEdge extends Function {

	public GetEdge() {
		super("Returns the $graph$'s edge with the specified $id$.",
				Category.GRAPH);
	}

	public Edge evaluate(Graph graph, Integer id) {
		return graph.getEdge(id);
	}
}
