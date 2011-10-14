package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;

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
		GreqlEvaluator greqlEvaluator = event.getGraph().getECARuleManager()
				.getGreqlEvaluator();
		if (this.conditionExpression.contains("context")) {
			greqlEvaluator.setQuery("using context: " + conditionExpression);
			JValue jva;
			if (element instanceof Vertex) {
				jva = new JValueImpl((Vertex) element);
			} else if (element instanceof Edge) {
				jva = new JValueImpl((Edge) element);
			} else {
				jva = new JValueImpl((Graph) element);
			}
			greqlEvaluator.setVariable("context", jva);

		} else {
			greqlEvaluator.setQuery(this.conditionExpression);
		}
		greqlEvaluator.startEvaluation();
		JValue result = greqlEvaluator.getEvaluationResult();
		if (result.isBoolean()) {
			return result.toBoolean();
		} else {
			System.err
					.println("Invalid Condition: " + this.conditionExpression);
			throw new ECAException("Invalid Condition: \""
					+ this.conditionExpression + "\" evaluates to JValueType "
					+ result.getType() + " but the result has to be a boolean.");
		}
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
