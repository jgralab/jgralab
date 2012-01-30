package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public interface Action<AEC extends AttributedElementClass<AEC, ?>> {
	/**
	 * Executes the action
	 */
	public void doAction(Event<AEC> event);
}
