package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;

public class ChangeAttributeEvent extends Event {

	private AttributedElement element;
	private String attributeName;
	private Object oldValue;
	private Object newValue;

	public ChangeAttributeEvent(int nestedCalls,
			EventDescription.EventTime time, Graph graph,
			AttributedElement element,
			String attributeName,
			Object oldValue,
			Object newValue) {
		super(nestedCalls, time, graph);
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
