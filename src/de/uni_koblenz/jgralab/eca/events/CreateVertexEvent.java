package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.events.EventDescription.EventTime;

public class CreateVertexEvent extends Event {

	/**
	 * The created Vertex or null if the EventTime is before
	 */
	private Vertex vertex;

	/**
	 * Creates an CreateVertexEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param element
	 *            the created Vertex or null if the EventTime is before
	 */
	public CreateVertexEvent(int nestedCalls, Graph graph, Vertex element) {
		super(nestedCalls, EventTime.AFTER, graph, element.getSchemaClass());
		vertex = element;
	}

	/**
	 * Creates an CreateVertexEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param type
	 *            the type of the to be created Vertex
	 */
	public CreateVertexEvent(int nestedCalls, Graph graph,
			Class<? extends AttributedElement<?, ?>> type) {
		super(nestedCalls, EventTime.BEFORE, graph, type);
		vertex = null;
	}

	/**
	 * @return the created Vertex or null if the EventTime is before
	 */
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * @return the AttributedElement that causes the Event or null if the
	 *         EventTime is before
	 */
	@Override
	public AttributedElement<?, ?> getElement() {
		return vertex;
	}

}
