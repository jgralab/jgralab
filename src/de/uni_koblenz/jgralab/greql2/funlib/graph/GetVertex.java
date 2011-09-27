package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.funlib.NeedsGraphArgument;

public class GetVertex extends Function implements NeedsGraphArgument {

	public GetVertex() {
		super("Returns the $graph$'s vertex with the specified $id$.",
				Category.GRAPH);
	}

	public Vertex evaluate(Graph graph, Integer id) {
		return graph.getVertex(id);
	}
}
