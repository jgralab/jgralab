package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

public class Condition {

	private String conditionExpression;
	private ECARule rule;

	public Condition(String condEx){
		this.conditionExpression = condEx;
	}
	
	public boolean evaluate(GraphElement element){
		//TODO find out how to give parameters with element
		GreqlEvaluator eval = new GreqlEvaluator(conditionExpression, element.getGraph(), null);
		eval.startEvaluation();
		JValue result = eval.getEvaluationResult();
		return result.toBoolean();
	}
	
	public ECARule getRule() {
		return rule;
	}

	public void setRule(ECARule rule) {
		this.rule = rule;
	}

	public String getConditionExpression() {
		return conditionExpression;
	}
	
}
