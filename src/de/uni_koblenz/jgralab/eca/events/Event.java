package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;

public abstract class Event {

	private int nestedCalls;

	private EventDescription.EventTime time;

	private Graph graph;

	public Event(int nC, EventDescription.EventTime time, Graph graph) {
		this.nestedCalls = nC;
		this.time = time;
		this.graph = graph;
	}

	public int getNestedCalls() {
		return nestedCalls;
	}

	public abstract AttributedElement getElement();

	public EventDescription.EventTime getTime() {
		return time;
	}

	public Graph getGraph() {
		return graph;
	}

}
