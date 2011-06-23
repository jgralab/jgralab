package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class ChangeEdgeEvent extends Event {

	private Edge edge;
	private Vertex oldVertex;
	private Vertex newVertex;

	public ChangeEdgeEvent(int nestedCalls, EventDescription.EventTime time,
			Edge edge, Vertex oldVertex,
			Vertex newVertex) {
		super(nestedCalls, time);

		this.edge = edge;
		this.oldVertex = oldVertex;
		this.newVertex = newVertex;
	}

	public Edge getEdge() {
		return edge;
	}

	public Vertex getOldVertex() {
		return oldVertex;
	}

	public Vertex getNewVertex() {
		return newVertex;
	}

	@Override
	public AttributedElement getElement() {
		return this.edge;
	}

}
