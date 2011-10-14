package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;

public class GreqlCondition implements Condition {
	/**
	 * Condition as GReQuL Query
	 */
	private String conditionExpression;

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a Condition with the given GReQuL Query as condition Expression
	 *
	 * @param conditionExpression
	 *            condition as GReQuL Query
	 */
	public GreqlCondition(String conditionExpression) {
		this.conditionExpression = conditionExpression;
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Evaluates the condition
	 *
	 * @param event
	 *            an Event containing the element to check the condition for
	 * @return if the condition is evaluated to true
	 */
	@Override
	public boolean evaluate(Event event) {
		AttributedElement element = event.getElement();
		GreqlEvaluator greqlEvaluator = ((ECARuleManager) event.getGraph()
				.getECARuleManager()).getGreqlEvaluator();
		if (this.conditionExpression.contains("context")) {
			greqlEvaluator.setQuery("using context: " + conditionExpression);
			greqlEvaluator.setVariable("context", element);
		} else {
			greqlEvaluator.setQuery(this.conditionExpression);
		}
		greqlEvaluator.startEvaluation();
		return (Boolean) greqlEvaluator.getResult();
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * @return the conditionExpression
	 */
	public String getConditionExpression() {
		return conditionExpression;
	}

	@Override
	public String toString() {
		return "Condition: " + this.conditionExpression;
	}

}
