package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.events.EventDescription.EventTime;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class CreateEdgeEvent extends Event<EdgeClass> {

	/**
	 * The created Edge or null if the EventTime is before
	 */
	private Edge edge;

	/**
	 * Creates an CreateEdgeEvent with the given parameters, EventTime is after
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param edge
	 *            the created Edge or null if the EventTime is before
	 */
	public CreateEdgeEvent(int nestedCalls, Graph graph, Edge edge) {
		super(nestedCalls, EventTime.AFTER, graph, edge
				.getAttributedElementClass());
		this.edge = edge;
	}

	/**
	 * Creates an CreateEdgeEvent with the given parameters, EventTime is before
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param type
	 *            the type
	 */
	public CreateEdgeEvent(int nestedCalls, Graph graph, EdgeClass type) {
		super(nestedCalls, EventTime.BEFORE, graph, type);
		edge = null;
	}

	/**
	 * @return the AttributedElement that causes this Event or null if the
	 *         EventTime is before
	 */
	@Override
	public Edge getElement() {
		return edge;
	}

}
