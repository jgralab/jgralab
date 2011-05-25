package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.eca.EventManager;

public class CreateVertexEvent extends Event {

	public CreateVertexEvent(EventManager manager, EventTime time, Class <? extends AttributedElement> type) {
		super(manager, time,type);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeCreateVertexEvents().add(this);
		}else{
			manager.getAfterCreateVertexEvents().add(this);
		}
	}
	public CreateVertexEvent(EventManager manager, EventTime time, String contextExpr) {
		super(manager, time,contextExpr);
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
