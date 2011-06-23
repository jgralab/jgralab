package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.eca.events.Event;

public abstract class Action {

	/**
	 * Rule that owns this Action
	 */
	private ECARule rule;
	
	/**
	 * Executes the action
	 */
	public abstract void doAction(Event event);

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	public ECARule getRule() {
		return rule;
	}

	public void setRule(ECARule rule) {
		this.rule = rule;
	}
	
}
