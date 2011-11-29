package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.funlib.NeedsGraphArgument;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;

@NeedsGraphArgument
public class EdgeTypeSubgraph extends Function {

	public EdgeTypeSubgraph() {
		super(
				"Returns the subgraph induced by the edge type given.",
				7, 1, 1.0, Category.GRAPH);
	}

	public SubGraphMarker evaluate(Graph graph, TypeCollection typeCollection) {
		SubGraphMarker subgraphMarker = new SubGraphMarker(graph);
		Edge currentEdge = graph.getFirstEdge();
		while (currentEdge != null) {
			if (typeCollection.acceptsType(currentEdge
					.getAttributedElementClass())) {
				subgraphMarker.mark(currentEdge);
				subgraphMarker.mark(currentEdge.getAlpha());
				subgraphMarker.mark(currentEdge.getOmega());
			}
			currentEdge = currentEdge.getNextEdge();
		}
		return subgraphMarker;
	}
	
}
