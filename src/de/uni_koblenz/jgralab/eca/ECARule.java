package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.eca.events.EventDescription;

public class ECARule {

	/**
	 * ECARuleManager of this ECARule
	 */
	private ECARuleManager manager;

	/**
	 * Event part of ECARule
	 */
	private EventDescription eventDescription;

	/**
	 * Condition part of ECARule, optional
	 */
	private Condition condition;

	/**
	 * Action part of ECARule
	 */
	private Action action;

	// +++++ Constructor Summary +++++++++++++++++++++++++++

	/**
	 * Creates an ECARule with the given Event and Action
	 * 
	 * @param event
	 *            Event
	 * @param action
	 *            Action
	 */
	public ECARule(EventDescription event, Action action) {
		this.setEventDescription(event);
		this.setAction(action);
	}

	/**
	 * Creates an ECARule with the given Event, Condition and Action
	 * 
	 * @param event
	 *            Event
	 * @param condition
	 *            Condition
	 * @param action
	 *            Action
	 */
	public ECARule(EventDescription event, Condition condition, Action action) {
		this.setEventDescription(event);
		this.setCondition(condition);
		this.setAction(action);
	}

	// ++++++ Methods +++++++++++++++++++++++++++++++++++++

	/**
	 * Triggers the ECARule. If a concerned element exists, it can become used
	 * by the condition, if not, null is excepted as element. If there is no
	 * condition or the condition is evaluated to true, the action is executed.
	 * 
	 * @param event
	 *            an Event containing the concerned element
	 */
	public void trigger(Event event) {
		if (this.condition == null || this.condition.evaluate(event)) {
			this.action.doAction(event);
		}
	}

	// +++++ Getter and Setter +++++++++++++++++++++++++++++

	/**
	 * @return the Event
	 */
	public EventDescription getEventDescription() {
		return eventDescription;
	}

	/**
	 * Sets the Event part of the ECARule
	 * 
	 * @param event
	 *            the Event
	 */
	private void setEventDescription(EventDescription event) {
		this.eventDescription = event;
	}

	/**
	 * @return the Condition
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * Sets the Condition part of the ECARule
	 * 
	 * @param condition
	 *            the Condition
	 */
	private void setCondition(Condition condition) {
		this.condition = condition;
	}

	/**
	 * @return the Action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Sets the Action part of the ECARule
	 * 
	 * @param action
	 *            the Action
	 */
	private void setAction(Action action) {
		this.action = action;
	}

	/**
	 * Sets the ECARuleManager of this rule. The method is called by the
	 * addECARule Method of the ECARuleManager.
	 * 
	 * @param manager
	 *            the ECARulemanager that should manage this rule
	 */
	public void setECARuleManager(ECARuleManager manager) {
		this.manager = manager;
	}

	/**
	 * @return the ECARuleManager of this rule if there is one
	 */
	public ECARuleManager getECARuleManager() {
		return manager;
	}

}
