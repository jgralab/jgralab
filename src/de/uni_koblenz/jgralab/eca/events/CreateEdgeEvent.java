package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;

public class CreateEdgeEvent extends Event {

	private Edge edge;

	public CreateEdgeEvent(int nestedCalls, Edge edge) {
		super(nestedCalls);
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
