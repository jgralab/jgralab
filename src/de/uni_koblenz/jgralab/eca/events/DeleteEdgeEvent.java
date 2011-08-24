package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.events.EventDescription.EventTime;

public class DeleteEdgeEvent extends Event {

	/**
	 * The to be deleted Edge or null if the EventTime is after
	 */
	private Edge edge;

	/**
	 * Creates an DeleteEdgeEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param edge
	 *            the to be deleted Edge or null if the EventTime is after
	 */
	public DeleteEdgeEvent(int nestedCalls, Graph graph, Edge edge) {
		super(nestedCalls, EventTime.BEFORE, graph, edge.getM1Class());
		this.edge = edge;
	}

	/**
	 * Creates an DeleteEdgeEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param type
	 *            type of the deleted Edge
	 */
	public DeleteEdgeEvent(int nestedCalls, Graph graph,
			Class<? extends AttributedElement> type) {
		super(nestedCalls, EventTime.AFTER, graph, type);
		this.edge = null;
	}

	/**
	 * @return the to be deleted Edge or null if the EventTime is after
	 */
	public Edge getEdge() {
		return edge;
	}

	/**
	 * @return the AttributedElement that causes this Event or null if the
	 *         EventTime is after
	 */
	@Override
	public AttributedElement getElement() {
		return this.edge;
	}
}
