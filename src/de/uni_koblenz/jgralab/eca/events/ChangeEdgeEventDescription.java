package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Vertex;

public class ChangeEdgeEventDescription extends EventDescription {

	Vertex latestOldVertex;
	Vertex latesNewVertex;

	/**
	 * Creates an ChangeEdgeEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public ChangeEdgeEventDescription(EventTime time,
			Class<? extends AttributedElement> type) {
		super(time, type);
	}

	/**
	 * Creates an ChangeEdgeEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public ChangeEdgeEventDescription(EventTime time, String contextExpr) {
		super(time, contextExpr);
	}

	public void fire(AttributedElement element, Vertex oldVertex,
			Vertex newVertex) {
		this.latestOldVertex = oldVertex;
		this.latesNewVertex = newVertex;
		super.fire(element);
	}

	public Vertex getLatestOldVertex() {
		return latestOldVertex;
	}

	public Vertex getLatesNewVertex() {
		return latesNewVertex;
	}

}
