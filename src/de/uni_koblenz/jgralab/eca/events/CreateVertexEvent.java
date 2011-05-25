package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.eca.EventManager;

public class CreateVertexEvent extends Event {

	public CreateVertexEvent(EventManager manager, EventTime time) {
		super(manager, time);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeCreateVertexEvents().add(this);
		}else{
			manager.getAfterCreateVertexEvents().add(this);
		}
	}

	
	public void fire(Class<? extends Vertex> vertexClass){
		//Event must only have a type
		for(ECARule rule : rules){
			if(this.getType().equals(vertexClass)){
				rule.trigger(null);		
			}
		}
	}
}
