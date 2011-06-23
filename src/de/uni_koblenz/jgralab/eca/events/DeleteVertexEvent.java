package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class DeleteVertexEvent extends Event {

	private Vertex vertex;

	public DeleteVertexEvent(int nestedCalls, EventDescription.EventTime time,
			Graph graph,
			Vertex vertex) {
		super(nestedCalls, time, graph);
		this.vertex = vertex;
	}

	public Vertex getVertex() {
		return vertex;
	}

	@Override
	public AttributedElement getElement() {
		return this.vertex;
	}

}
