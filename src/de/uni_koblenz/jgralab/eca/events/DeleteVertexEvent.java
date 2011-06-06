package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;

public class DeleteVertexEvent extends Event {

	/**
	 * Creates an DeleteVertexEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public DeleteVertexEvent(EventTime time,
			Class<? extends AttributedElement> type) {
		super(time, type);
	}

	/**
	 * Creates an DeleteVertexEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public DeleteVertexEvent(EventTime time, String contextExpr) {
		super(time, contextExpr);
	}

	
}
