package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;

public abstract class Event {

	private int nestedCalls;

	public Event(int nC) {
		this.nestedCalls = nC;
	}

	public int getNestedCalls() {
		return nestedCalls;
	}

	public abstract AttributedElement getElement();

}
