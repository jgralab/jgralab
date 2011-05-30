package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.eca.events.Event;


public class ECARule {

	private Event event;
	private Condition condition;
	private Action action;
	
		
	//Constructor Summary 
	
	public ECARule(Event event, Action act){
		this.setEvent(event);
		this.setAction(act);
	}
	
	public ECARule(Event event, Condition cond, Action act){
		this.setEvent(event);
		this.setCondition(cond);
		this.setAction(act);
	}
	
	//Methods
	
	public void trigger(AttributedElement element){	
		if(this.condition == null || this.condition.evaluate(element)){
			this.action.doAction();
		}			
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
