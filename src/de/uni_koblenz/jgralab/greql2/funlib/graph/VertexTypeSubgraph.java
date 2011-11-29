package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.funlib.NeedsGraphArgument;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;

@NeedsGraphArgument
public class VertexTypeSubgraph extends Function {

	public VertexTypeSubgraph() {
		super(
				"Returns the subgraph induced by the vertex type given.",
				7, 1, 1.0, Category.GRAPH);
	}

	public SubGraphMarker evaluate(Graph graph, TypeCollection typeCollection) {
		SubGraphMarker subgraphMarker = new SubGraphMarker(graph);
		Vertex currentVertex = graph.getFirstVertex();
		while (currentVertex != null) {
			if (typeCollection.acceptsType(currentVertex
					.getAttributedElementClass())) {
				subgraphMarker.mark(currentVertex);
			}
			currentVertex = currentVertex.getNextVertex();
		}
		// add all edges
		Edge currentEdge = graph.getFirstEdge();
		while (currentEdge != null) {
			if (subgraphMarker.isMarked(currentEdge.getAlpha())
					&& subgraphMarker.isMarked(currentEdge.getOmega())) {
				subgraphMarker.mark(currentEdge);
			}
			currentEdge = currentEdge.getNextEdge();
		}
		return subgraphMarker;
	}
	
}
