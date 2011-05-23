package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.eca.EventManager;

public class ChangeEdgeEvent extends Event {

	public ChangeEdgeEvent(EventManager manager, EventTime time) {
		super(manager, time);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeChangeEdgeEvents().add(this);
		}else{
			manager.getAfterChangeEdgeEvents().add(this);
		}
	}

}
