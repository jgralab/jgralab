package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class CreateVertexEvent extends Event {

	private Vertex vertex;

	public CreateVertexEvent(int nestedCalls, EventDescription.EventTime time,
			Graph graph,
			Vertex element) {
		super(nestedCalls, time, graph);

		this.vertex = element;
	}

	public Vertex getVertex() {
		return vertex;
	}

	@Override
	public AttributedElement getElement() {
		return this.vertex;
	}

}
