package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.events.EventDescription.EventTime;

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
	 * @param graph
	 *            Graph where the Event happened
	 * @param vertex
	 *            the to be deleted Vertex or null if the EventTime is after
	 */
	public DeleteVertexEvent(int nestedCalls, Graph graph, Vertex vertex) {
		super(nestedCalls, EventTime.BEFORE, graph, vertex.getM1Class());
		this.vertex = vertex;
	}

	/**
	 * Creates an DeleteVertexEvent with the given parameters
	 * 
	 * @param nestedCallsdepth
	 *            of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param type
	 *            the type of the deleted Vertex
	 */
	public DeleteVertexEvent(int nestedCalls, Graph graph,
			Class<? extends AttributedElement> type) {
		super(nestedCalls, EventTime.AFTER, graph, type);
		this.vertex = null;
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
