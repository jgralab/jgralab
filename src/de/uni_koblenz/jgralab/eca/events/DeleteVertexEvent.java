package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Vertex;

public class DeleteVertexEvent extends Event {

	private Vertex vertex;

	public DeleteVertexEvent(int nestedCalls, Vertex vertex) {
		super(nestedCalls);
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
