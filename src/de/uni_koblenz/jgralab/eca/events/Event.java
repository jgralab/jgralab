package de.uni_koblenz.jgralab.eca.events;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.eca.EventManager;

public abstract class Event {

	private EventManager manager;
	private List<ECARule> rules;
	
	private EventTime time;
	
	public enum EventTime{
		BEFORE,
		AFTER
	}
	
	public Event(EventManager manager, EventTime time){
		this.manager = manager;
		this.time = time;
		this.rules = new ArrayList<ECARule>();
	}
	
	public void fire(GraphElement element){
		for(ECARule rule : rules){
			rule.trigger(element);
		}
	}

	//getter und setter
	public EventTime getTime() {
		return time;
	}
	
	public void addRule(ECARule rule){
		this.rules.add(rule);
	}
}
