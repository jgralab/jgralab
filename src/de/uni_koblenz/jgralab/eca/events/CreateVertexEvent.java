package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Vertex;

public class CreateVertexEvent extends Event {

	private Vertex vertex;

	public CreateVertexEvent(int nestedCalls, Vertex vertex) {
		super(nestedCalls);

		this.vertex = vertex;
	}

	public Vertex getVertex() {
		return vertex;
	}

}
