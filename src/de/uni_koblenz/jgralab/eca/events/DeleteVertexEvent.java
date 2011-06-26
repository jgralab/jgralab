package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class DeleteVertexEvent extends Event {

	/**
	 * The to be deleted Vertex or null if the EventTime is after
	 */
	private Vertex vertex;

	/**
	 * Creates an DeleteVertexEvent with the given parameters
	 * 
	 * @param nestedCallsdepth
	 *            of nested trigger calls
	 * @param time
	 *            before or after
	 * @param graph
	 *            Graph where the Event happened
	 * @param vertex
	 *            the to be deleted Vertex or null if the EventTime is after
	 */
	public DeleteVertexEvent(int nestedCalls, EventDescription.EventTime time,
			Graph graph,
			Vertex vertex) {
		super(nestedCalls, time, graph);
		this.vertex = vertex;
	}

	/**
	 * @return the to be deleted Vertex or null if the EventTime is after
	 */
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * @return the AttributedElement that causes this Event or null if the
	 *         EventTime is after
	 */
	@Override
	public AttributedElement getElement() {
		return this.vertex;
	}

}
