package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
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

	/**
	 * GReQL Evaluator to evaluate the condition
	 */
	private GreqlEvaluator greqlEvaluator;

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a Condition with the given GReQuL Query as condition Expression
	 * 
	 * @param conditionExpression
	 *            condition as GReQuL Query
	 */
	public Condition(String conditionExpression){
		this.conditionExpression = conditionExpression;
		this.greqlEvaluator = new GreqlEvaluator("", null, null);
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

		if (!this.greqlEvaluator.getDatagraph().equals(
				this.rule.getECARuleManager().getGraph())) {
			this.greqlEvaluator.setDatagraph(this.rule.getECARuleManager()
					.getGraph());
		}

		if (this.conditionExpression.contains("context")) {
			this.greqlEvaluator.setQuery("using context: "
					+ conditionExpression);
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
			this.greqlEvaluator.setQuery(this.conditionExpression);
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
	
	public GreqlEvaluator getGreqlEvaluator() {
		return greqlEvaluator;
	}

	@Override
	public String toString() {
		return "Condition: " + this.conditionExpression;
	}

}
