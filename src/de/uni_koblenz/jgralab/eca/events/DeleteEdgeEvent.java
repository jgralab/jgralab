package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;

public class DeleteEdgeEvent extends Event {

	/**
	 * The to be deleted Edge or null if the EventTime is after
	 */
	private Edge edge;

	/**
	 * Creates an DeleteEdgeEvent with the given parameters
	 * 
	 * @param nestedCallsdepth
	 *            of nested trigger calls
	 * @param time
	 *            before or after
	 * @param graph
	 *            Graph where the Event happened
	 * @param edge
	 *            the to be deleted Edge or null if the EventTime is after
	 */
	public DeleteEdgeEvent(int nestedCalls, EventDescription.EventTime time,
			Graph graph,
			Edge edge) {
		super(nestedCalls, time, graph);
		this.edge = edge;
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
