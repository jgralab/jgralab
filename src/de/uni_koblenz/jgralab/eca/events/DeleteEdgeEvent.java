package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.events.EventDescription.EventTime;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class DeleteEdgeEvent extends Event<EdgeClass> {

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
		super(nestedCalls, EventTime.BEFORE, graph, edge
				.getAttributedElementClass());
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
	public DeleteEdgeEvent(int nestedCalls, Graph graph, EdgeClass type) {
		super(nestedCalls, EventTime.AFTER, graph, type);
		edge = null;
	}

	/**
	 * @return the AttributedElement that causes this Event or null if the
	 *         EventTime is after
	 */
	@Override
	public Edge getElement() {
		return edge;
	}
}
