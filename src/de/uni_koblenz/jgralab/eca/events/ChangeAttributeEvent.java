package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.EventManager;

public class ChangeAttributeEvent extends Event {

	/**
	 * Name of the Attribute, this Event monitors changes
	 */
	private String concernedAttribute;

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a ChangeAttributeEvent with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors for Attribute
	 *            changes
	 * @param attributeName
	 *            the name of the observed Attribute
	 */
	public ChangeAttributeEvent(EventManager manager, EventTime time,
			Class<? extends AttributedElement> type, String attributeName) {
		super(manager,time,type);
		this.concernedAttribute = attributeName;
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeChangeAttributeEvents().add(this);
		}else{
			manager.getAfterChangeAttributeEvents().add(this);
		}		
	}
	
	/**
	 * Creates a ChangeAttributeEvent with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExr
	 *            the GReQuL-Expression that represents the context of this
	 *            Event
	 * @param attributeName
	 *            the name of the observed Attribute
	 */
	public ChangeAttributeEvent(EventManager manager, EventTime time,
			String contextExpr, String attributeName) {
		super(manager,time,contextExpr);
		this.concernedAttribute = attributeName;
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeChangeAttributeEvents().add(this);
		}else{
			manager.getAfterChangeAttributeEvents().add(this);
		}		
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Compares the Attribute names and calls in the case of Equality the Events
	 * fire method
	 * 
	 * @param element
	 *            the AttributedElement an Attribute will change or changed for
	 * @param attributeName
	 *            the name of the changing or changed Attribute
	 */
	public void fire(AttributedElement element, String attributeName){
		if(concernedAttribute.equals(attributeName)){
			this.fire(element);
		}
	}
	
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * @return the name of the monitored Attribute
	 */
	public String getConcernedAttribute() {
		return concernedAttribute;
	}

	
	
}
