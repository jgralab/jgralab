package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.ECARule;

public class ChangeAttributeEventDescription extends EventDescription {

	/**
	 * Name of the Attribute, this Event monitors changes
	 */
	private String concernedAttribute;


	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a ChangeAttributeEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors for Attribute
	 *            changes
	 * @param attributeName
	 *            the name of the observed Attribute
	 */
	public ChangeAttributeEventDescription(EventTime time,
			Class<? extends AttributedElement> type, String attributeName) {
		super(time, type);
		this.concernedAttribute = attributeName;
	}
	
	/**
	 * Creates a ChangeAttributeEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExr
	 *            the GReQuL-Expression that represents the context of this
	 *            Event
	 * @param attributeName
	 *            the name of the observed Attribute
	 */
	public ChangeAttributeEventDescription(EventTime time,
			String contextExpr, String attributeName) {
		super(time, contextExpr);
		this.concernedAttribute = attributeName;
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
	public void fire(AttributedElement element, String attributeName,
			Object oldValue, Object newValue) {
		if(concernedAttribute.equals(attributeName)){
			if (super.checkContext(element)) {
				int nested = this.getActiveECARules().get(0)
						.getECARuleManager().getNestedTriggerCalls();
				Graph graph = this.getActiveECARules().get(0)
						.getECARuleManager().getGraph();
				for (ECARule rule : activeRules) {
					rule.trigger(new ChangeAttributeEvent(nested, this
							.getTime(), graph, element,
							attributeName,
							oldValue,
							newValue));
				}
			}
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
