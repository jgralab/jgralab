package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.schema.GraphElementClass;

public class ECARule {

	private Event event;
	private Condition condition;
	private Action action;
	
	private String contextExpression;
	private GraphElementClass type;
	private Context context;
	
	private enum Context{
		TYPE,
		EXPRESSION
	}
	
	//Constructor Summary 
	
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
	}
	
	
	//Methods
	
	public void trigger(GraphElement element){
		if(this.checkContext(element)){
			if(this.condition == null || this.condition.evaluate(element)){
				this.action.doAction();
			}
		}	
	}
	
	private boolean checkContext(GraphElement element){
		boolean result = false;
		if(this.context.equals(Context.TYPE)){
			if(element.getM1Class().equals(this.type)){
				return true;
			}
			else{
				return false;
			}
		}else{
			//TODO find out how grequl transformations work
			GreqlEvaluator eval = new GreqlEvaluator(this.contextExpression, element.getGraph(), null);
			eval.startEvaluation();
			JValue resultingContext = eval.getEvaluationResult();
			
		}
		return result;
	}
	
	//Getter und Setter
	
	public Event getEvent() {
		return event;
	}

	private void setEvent(Event event) {
		this.event = event;
		this.event.addRule(this);
	}
	
	public Condition getCondition() {
		return condition;
	}
	public void setCondition(Condition condition) {
		this.condition = condition;
		condition.setRule(this);
	}
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	public String getContextExpression() {
		return contextExpression;
	}
	public GraphElementClass getType() {
		return type;
	}
	public Context getContext() {
		return context;
	}
	
}
