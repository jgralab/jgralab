package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;

public class ChangeAttributeEvent extends Event {

	private AttributedElement element;
	private Object oldValue;
	private Object newValue;

	public ChangeAttributeEvent(int nestedCalls, AttributedElement element,
			Object oldValue,
			Object newValue) {
		super(nestedCalls);
		this.element = element;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public AttributedElement getElement() {
		return element;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

}
