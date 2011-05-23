package de.uni_koblenz.jgralab.eca.events;

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

}
