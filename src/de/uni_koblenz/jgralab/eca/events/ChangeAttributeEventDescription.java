package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class ChangeAttributeEventDescription<AEC extends AttributedElementClass<AEC, ?>>
		extends EventDescription<AEC> {

	/**
	 * Name of the Attribute, this EventDescription monitors changes
	 */
	private String concernedAttribute;

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a ChangeAttributeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this EventDescription monitors for
	 *            Attribute changes
	 * @param attributeName
	 *            the name of the observed Attribute
	 */
	public ChangeAttributeEventDescription(EventTime time, AEC type,
			String attributeName) {
		super(time, type);
		concernedAttribute = attributeName;
	}

	/**
	 * Creates a ChangeAttributeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the GReQuL-Expression that represents the context of this
	 *            EventDescription
	 * @param attributeName
	 *            the name of the observed Attribute
	 */
	public ChangeAttributeEventDescription(EventTime time, String contextExpr,
			String attributeName) {
		super(time, contextExpr);
		concernedAttribute = attributeName;
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Compares the Attribute names and context or type, triggers the rules if
	 * the EventDescription matches the Event
	 * 
	 * @param element
	 *            the AttributedElement an Attribute will change or changed for
	 * @param attributeName
	 *            the name of the changing or changed Attribute
	 */
	public void fire(AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
		if (concernedAttribute.equals(attributeName)) {
			if (super.checkContext(element)) {
				int nested = getActiveECARules().get(0).getECARuleManager()
						.getNestedTriggerCalls();
				Graph graph = getActiveECARules().get(0).getECARuleManager()
						.getGraph();
				for (ECARule<AEC> rule : activeRules) {
					rule.trigger(new ChangeAttributeEvent<AEC>(nested,
							getTime(), graph, element, attributeName, oldValue,
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
