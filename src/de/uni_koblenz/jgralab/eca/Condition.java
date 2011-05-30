package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;

public class Condition {

	private String conditionExpression;
	private ECARule rule;

	public Condition(String condEx){
		this.conditionExpression = condEx;
	}
	
	public boolean evaluate(AttributedElement element){
		Graph graph = rule.getEvent().getEventManager().getGraph();
		GreqlEvaluator greqlEvaluator = new GreqlEvaluator(conditionExpression, graph , null);		
		if(this.conditionExpression.contains("using v")){
			greqlEvaluator.setVariable("v", new JValueImpl(element)); 
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
