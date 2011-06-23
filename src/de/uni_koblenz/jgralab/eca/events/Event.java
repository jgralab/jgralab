package de.uni_koblenz.jgralab.eca.events;

public class Event {

	private int nestedCalls;

	public Event(int nC) {
		this.nestedCalls = nC;
	}

	public int getNestedCalls() {
		return nestedCalls;
	}

}
