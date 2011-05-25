package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.eca.EventManager;

public class CreateEdgeEvent extends Event{

	public CreateEdgeEvent(EventManager manager, EventTime time, Class <? extends AttributedElement> type) {
		super(manager,time,type);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeCreateEdgeEvents().add(this);
		}else{
			manager.getAfterCreateEdgeEvents().add(this);
		}
	}
	public CreateEdgeEvent(EventManager manager, EventTime time, String contextExpr) {
		super(manager,time,contextExpr);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeCreateEdgeEvents().add(this);
		}else{
			manager.getAfterCreateEdgeEvents().add(this);
		}
	}
	
	public void fire(Class<? extends Edge> edgeClass){
		for(ECARule rule : rules){
			if(this.getType().equals(edgeClass)){
				rule.trigger(null);		
			}
		}
	}
	
}
