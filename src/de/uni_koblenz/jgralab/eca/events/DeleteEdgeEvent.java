package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;

public class DeleteEdgeEvent extends Event {

	/**
	 * Creates an DeleteEdgeEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public DeleteEdgeEvent(EventTime time,
			Class<? extends AttributedElement> type) {
		super(time, type);
	}

	/**
	 * Creates an DeleteEdgeEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public DeleteEdgeEvent(EventTime time, String contextExpr) {
		super(time, contextExpr);
	}


}
