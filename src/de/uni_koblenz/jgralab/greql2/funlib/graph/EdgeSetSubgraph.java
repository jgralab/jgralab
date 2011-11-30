package de.uni_koblenz.jgralab.greql2.funlib.graph;

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.funlib.NeedsGraphArgument;

@NeedsGraphArgument
public class EdgeSetSubgraph extends Function {

	public EdgeSetSubgraph() {
		super(
				"Returns the subgraph induced by the edge set given.",
				7, 1, 1.0, Category.GRAPH);
	}

	public SubGraphMarker evaluate(Graph graph, PCollection<Edge> edgeSet) {
		SubGraphMarker subgraphMarker = new SubGraphMarker(graph);
		Edge currentEdge = graph.getFirstEdge();
		while (currentEdge != null) {
			if (edgeSet.contains(currentEdge)) {
				subgraphMarker.mark(currentEdge);
				subgraphMarker.mark(currentEdge.getAlpha());
				subgraphMarker.mark(currentEdge.getOmega());
			}
			currentEdge = currentEdge.getNextEdge();
		}
		return subgraphMarker;
	}
	
}
