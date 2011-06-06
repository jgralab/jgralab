package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;

public class Condition {

	/**
	 * Condition as GReQuL Query
	 */
	private String conditionExpression;

	/**
	 * Corresponding ECARule
	 */
	private ECARule rule;

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a Condition with the given GReQuL Query as condition Expression
	 * 
	 * @param condEx
	 *            condition as GReQuL Query
	 */
	public Condition(String condEx){
		this.conditionExpression = condEx;
	}
	
	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Evaluates the condition
	 * 
	 * @param element
	 *            the element to check the condition for
	 * @return if the condition is evaluated to true
	 */
	public boolean evaluate(AttributedElement element){
		Graph graph = rule.getECARuleManager().getGraph();
		GreqlEvaluator greqlEvaluator;
		if (this.conditionExpression.contains("context")) {
			greqlEvaluator = new GreqlEvaluator("using context: "
					+ conditionExpression, graph, null);
			greqlEvaluator.setVariable("context", new JValueImpl(element));
		} else {
			greqlEvaluator = new GreqlEvaluator(conditionExpression, graph,
					null);
		}
		greqlEvaluator.startEvaluation();
		JValue result = greqlEvaluator.getEvaluationResult();
		if(result.isBoolean()){
			return result.toBoolean();
		}
		else{
			System.err.println("Invalid Condition: "+this.conditionExpression);
			return false;
		}		
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * @return the corresponding ECARule of the condition
	 */
	public ECARule getRule() {
		return rule;
	}

	/**
	 * Sets the ECARule of the Condition
	 * 
	 * @param rule
	 *            the new ECARule
	 */
	public void setRule(ECARule rule) {
		this.rule = rule;
	}

	/**
	 * @return the conditionExpression
	 */
	public String getConditionExpression() {
		return conditionExpression;
	}
	
}
