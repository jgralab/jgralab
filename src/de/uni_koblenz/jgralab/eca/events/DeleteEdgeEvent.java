package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;

public class DeleteEdgeEvent extends Event {

	private Edge edge;

	public DeleteEdgeEvent(int nestedCalls, EventDescription.EventTime time,
			Graph graph,
			Edge edge) {
		super(nestedCalls, time, graph);
		this.edge = edge;
	}

	public Edge getEdge() {
		return edge;
	}

	@Override
	public AttributedElement getElement() {
		return this.edge;
	}
}
