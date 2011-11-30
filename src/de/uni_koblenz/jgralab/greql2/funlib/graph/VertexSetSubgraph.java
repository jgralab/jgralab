package de.uni_koblenz.jgralab.greql2.funlib.graph;

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.funlib.NeedsGraphArgument;

@NeedsGraphArgument
public class VertexSetSubgraph extends Function {

	public VertexSetSubgraph() {
		super(
				"Returns the subgraph induced by the vertex type given.",
				7, 1, 1.0, Category.GRAPH);
	}

	public SubGraphMarker evaluate(Graph graph, PCollection<Vertex> vertexSet) {
		SubGraphMarker subgraphMarker = new SubGraphMarker(graph);
		for (Vertex currentVertex : vertexSet) {
			subgraphMarker.mark(currentVertex);
		}
		// add all edges
		for (Vertex currentVertex : vertexSet) {
			Edge currentEdge = currentVertex.getFirstIncidence(EdgeDirection.OUT);
			while (currentEdge != null) {
				if (subgraphMarker.isMarked(currentEdge.getThat())) {
					subgraphMarker.mark(currentEdge);
				}
				currentEdge = currentEdge.getNextIncidence(EdgeDirection.OUT);
			}
		}
		
		return subgraphMarker;
	}
	
}
