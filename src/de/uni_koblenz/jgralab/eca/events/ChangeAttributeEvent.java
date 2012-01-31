package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class ChangeAttributeEvent<AEC extends AttributedElementClass<AEC, ?>>
		extends Event<AEC> {

	/**
	 * AttributedElement who causes this Event
	 */
	private AttributedElement<AEC, ?> element;

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
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
		super(nestedCalls, time, graph, element.getAttributedElementClass());
		this.element = element;
		this.attributeName = attributeName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * @return the AttributedElement that causes this Event
	 */
	@Override
	public AttributedElement<AEC, ?> getElement() {
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
