package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;

public class ChangeAttributeEvent extends Event {

	private AttributedElement element;
	private String attributeName;
	private Object oldValue;
	private Object newValue;

	public ChangeAttributeEvent(int nestedCalls,
			EventDescription.EventTime time, AttributedElement element,
			String attributeName,
			Object oldValue,
			Object newValue) {
		super(nestedCalls, time);
		this.element = element;
		this.attributeName = attributeName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public AttributedElement getElement() {
		return element;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

}
