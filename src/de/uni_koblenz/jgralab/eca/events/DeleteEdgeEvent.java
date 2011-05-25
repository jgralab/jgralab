package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.eca.EventManager;

public class DeleteEdgeEvent extends Event {

	public DeleteEdgeEvent(EventManager manager,EventTime time) {
		super(manager,time);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeDeleteEdgeEvents().add(this);
		}else{
			manager.getAfterDeleteEdgeEvents().add(this);
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
