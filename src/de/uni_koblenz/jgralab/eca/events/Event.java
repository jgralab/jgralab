package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;

public abstract class Event {

	private int nestedCalls;

	private EventDescription.EventTime time;

	public Event(int nC, EventDescription.EventTime time) {
		this.nestedCalls = nC;
		this.time = time;
	}

	public int getNestedCalls() {
		return nestedCalls;
	}

	public abstract AttributedElement getElement();

	public EventDescription.EventTime getTime() {
		return time;
	}

}
