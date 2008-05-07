package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class VertexEdgeEntry {
	
	VertexClass vertex;
	
	EdgeClass edge;
	
	boolean redefined;
	
	EdgeDirection direction;

	public VertexEdgeEntry(VertexClass vertex, DirectedEdgeClass edge,
			boolean redefined) {
		super();
		this.vertex = vertex;
		this.edge = edge.getEdgeClass();
		this.direction = edge.getDirection();
		this.redefined = redefined;
	}

	public VertexClass getVertex() {
		return vertex;
	}

	public EdgeClass getEdge() {
		return edge;
	}

	public boolean isRedefined() {
		return redefined;
	}
	
	public EdgeDirection getDirection() {
		return direction;
	}
	
	public void setRedefined(boolean redefined) {
		this.redefined = redefined;
	}
	
}