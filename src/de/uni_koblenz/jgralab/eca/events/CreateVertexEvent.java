package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class CreateVertexEvent extends Event {

	/**
	 * The created Vertex or null if the EventTime is before
	 */
	private Vertex vertex;

	/**
	 * Creates an CreateVertexEvent with the given parameters
	 * 
	 * @param nestedCallsdepth
	 *            of nested trigger calls
	 * @param time
	 *            before or after
	 * @param graph
	 *            Graph where the Event happened
	 * @param element
	 *            the created Vertex or null if the EventTime is before
	 */
	public CreateVertexEvent(int nestedCalls, EventDescription.EventTime time,
			Graph graph,
			Vertex element) {
		super(nestedCalls, time, graph);

		this.vertex = element;
	}

	/**
	 * @return the created Vertex or null if the EventTime is before
	 */
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * @return the AttributedElement that causes the Event or null if the
	 *         EventTime is before
	 */
	@Override
	public AttributedElement getElement() {
		return this.vertex;
	}

}
