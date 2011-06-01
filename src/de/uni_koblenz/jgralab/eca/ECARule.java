package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.events.Event;


public class ECARule {

	/**
	 * Event part of ECARule
	 */
	private Event event;

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
	public ECARule(Event event, Action action) {
		this.setEvent(event);
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
	public ECARule(Event event, Condition condition, Action action) {
		this.setEvent(event);
		this.setCondition(condition);
		this.setAction(action);
	}
	
	// ++++++ Methods +++++++++++++++++++++++++++++++++++++

	/**
	 * Triggers the ECARule. If a concerned element exists, it can become used
	 * by the condition, if not, null is excepted as element. If there is no
	 * condition or the condition is evaluated to true, the action is executed.
	 * 
	 * @param element
	 *            the AttributedElement to check the condition for, null if
	 *            there is none
	 */
	public void trigger(AttributedElement element){	
		if(this.condition == null || this.condition.evaluate(element)){
			this.action.doAction();
		}			
	}

	
	// +++++ Getter and Setter +++++++++++++++++++++++++++++
	
	/**
	 * @return the Event
	 */
	public Event getEvent() {
		return event;
	}

	/**
	 * Sets the Event part of the ECARule
	 * 
	 * @param event
	 *            the Event
	 */
	private void setEvent(Event event) {
		this.event = event;
		this.event.addRule(this);
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
	public void setCondition(Condition condition) {
		this.condition = condition;
		condition.setRule(this);
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
	public void setAction(Action action) {
		this.action = action;
	}
	
	/**
	public ECARule(String contextExpression, Event event, Action act){
		this.contextExpression = contextExpression;
		this.context = Context.EXPRESSION;
		this.setEvent(event);
		this.setAction(act);
	}
	
	public ECARule(GraphElementClass type, Event event, Action act){
		this.type = type;
		this.context = Context.TYPE;
		this.setEvent(event);
		this.setAction(act);
	}
	
	public ECARule(String contextExpression, Event event, Condition cond, Action act){
		this.contextExpression = contextExpression;
		this.context = Context.EXPRESSION;
		this.setEvent(event);
		this.setCondition(cond);
		this.setAction(act);
	}
	
	public ECARule(GraphElementClass type, Event event, Condition cond, Action act){
		this.type = type;
		this.context = Context.TYPE;
		this.setEvent(event);
		this.setCondition(cond);
		this.setAction(act);
	}*/
}
