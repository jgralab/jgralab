package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.EventManager;

public class ChangeEdgeEvent extends Event {

	public ChangeEdgeEvent(EventManager manager, EventTime time,Class <? extends AttributedElement> type) {
		super(manager, time,type);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeChangeEdgeEvents().add(this);
		}else{
			manager.getAfterChangeEdgeEvents().add(this);
		}
	}
	public ChangeEdgeEvent(EventManager manager, EventTime time,String contextExpr) {
		super(manager, time,contextExpr);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeChangeEdgeEvents().add(this);
		}else{
			manager.getAfterChangeEdgeEvents().add(this);
		}
	}

}
