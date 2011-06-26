package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;

public class CreateEdgeEvent extends Event {

	/**
	 * The created Edge or null if the EventTime is before
	 */
	private Edge edge;

	/**
	 * Creates an CreateEdgeEvent with the given parameters
	 * 
	 * @param nestedCallsdepth
	 *            of nested trigger calls
	 * @param time
	 *            before or after
	 * @param graph
	 *            Graph where the Event happened
	 * @param edge
	 *            the created Edge or null if the EventTime is before
	 */
	public CreateEdgeEvent(int nestedCalls, EventDescription.EventTime time,
			Graph graph,
			Edge edge) {
		super(nestedCalls, time, graph);
		this.edge = edge;
	}

	/**
	 * @return the created Edge or null if the EventTime is before
	 */
	public Edge getEdge() {
		return edge;
	}

	/**
	 * @return the AttributedElement that causes this Event or null if the
	 *         EventTime is before
	 */
	@Override
	public AttributedElement getElement() {
		return this.edge;
	}

}
