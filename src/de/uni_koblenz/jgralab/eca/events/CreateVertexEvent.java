package de.uni_koblenz.jgralab.eca.events;

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

}
