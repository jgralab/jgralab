package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.eca.events.Event;

public interface Condition {

	/**
	 * Evaluates the condition
	 *
	 * @param event
	 *            an Event containing the element to check the condition for
	 * @return if the condition is evaluated to true
	 */
	public boolean evaluate(Event event);
}
