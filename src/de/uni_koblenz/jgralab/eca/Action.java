package de.uni_koblenz.jgralab.eca;

public abstract class Action {

	/**
	 * Rule that owns this Action
	 */
	private ECARule rule;
	
	/**
	 * Executes the action
	 */
	public void doAction() {
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	public ECARule getRule() {
		return rule;
	}

	public void setRule(ECARule rule) {
		this.rule = rule;
	}
	
}
