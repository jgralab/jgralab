package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.eca.EventManager;

public class CreateEdgeEvent extends Event{

	public CreateEdgeEvent(EventManager manager, EventTime time) {
		super(manager,time);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeCreateEdgeEvents().add(this);
		}else{
			manager.getAfterCreateEdgeEvents().add(this);
		}
	}

}
