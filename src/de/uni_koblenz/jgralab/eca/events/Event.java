package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;

public abstract class Event {

	/**
	 * The nested call depth, the Event happened
	 */
	private int nestedCalls;

	/**
	 * The time the Event happened, before or after
	 */
	private EventDescription.EventTime time;

	/**
	 * The Graph where the Event happened
	 */
	private Graph graph;

	/**
	 * Create an Event with the given parameters
	 * 
	 * @param nC
	 *            depth of nested trigger calls
	 * @param time
	 *            before or after
	 * @param graph
	 *            Graph where the Event happened
	 */
	public Event(int nC, EventDescription.EventTime time, Graph graph) {
		this.nestedCalls = nC;
		this.time = time;
		this.graph = graph;
	}

	/**
	 * @return depth of nested Calls
	 */
	public int getNestedCalls() {
		return nestedCalls;
	}

	/**
	 * @return the element who causes the Event, can be Vertex, Edge or Graph
	 */
	public abstract AttributedElement getElement();

	/**
	 * @return if the Event happened before or after
	 */
	public EventDescription.EventTime getTime() {
		return time;
	}

	/**
	 * @return the Graph where the Event happened
	 */
	public Graph getGraph() {
		return graph;
	}

}
