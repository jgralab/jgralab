package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;

public class ChangeAttributeEvent extends Event {

	/**
	 * AttributedElement who causes this Event
	 */
	private AttributedElement element;

	/**
	 * Name of the Attribute that changes
	 */
	private String attributeName;

	/**
	 * Old value of the Attribute
	 */
	private Object oldValue;

	/**
	 * New value of the Attribute
	 */
	private Object newValue;

	/**
	 * Creates a new ChangeAttributeEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested calls
	 * @param time
	 *            before or after
	 * @param graph
	 *            Graph where the Event occurs
	 * @param element
	 *            AttributedElement that causes the Event
	 * @param attributeName
	 *            name of the changing Attribute
	 * @param oldValue
	 *            old value of the Attribute
	 * @param newValue
	 *            new value of the Attribute
	 */
	public ChangeAttributeEvent(int nestedCalls,
			EventDescription.EventTime time, Graph graph,
			AttributedElement element, String attributeName, Object oldValue,
			Object newValue) {
		super(nestedCalls, time, graph, element.getSchemaClass());
		this.element = element;
		this.attributeName = attributeName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * @return the AttributedElement that causes this Event
	 */
	@Override
	public AttributedElement getElement() {
		return element;
	}

	/**
	 * @return the name of the changing Attribute
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @return the old value of the Attribute
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * @return the new value of the Attribute
	 */
	public Object getNewValue() {
		return newValue;
	}

}
