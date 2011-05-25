package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.eca.EventManager;

public class DeleteVertexEvent extends Event {

	public DeleteVertexEvent(EventManager manager, EventTime time, Class <? extends AttributedElement> type) {
		super(manager,time,type);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeDeleteVertexEvents().add(this);
		}else{
			manager.getAfterDeleteVertexEvents().add(this);
		}
	}
	public DeleteVertexEvent(EventManager manager, EventTime time, String contextExpr) {
		super(manager,time,contextExpr);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeDeleteVertexEvents().add(this);
		}else{
			manager.getAfterDeleteVertexEvents().add(this);
		}
	}

	//for after delete
	public void fire(Class <? extends AttributedElement> atClass){
		for(ECARule rule : rules){
			if(this.getType().equals(atClass)){
				rule.trigger(null);		
			}
		}
	}
	
}
