package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.EventManager;

public class CreateEdgeEvent extends Event{

	/**
	 * Creates an CreateEdgeEvent with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public CreateEdgeEvent(EventManager manager, EventTime time, Class <? extends AttributedElement> type) {
		super(manager,time,type);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeCreateEdgeEvents().add(this);
		}else{
			manager.getAfterCreateEdgeEvents().add(this);
		}
	}

	/**
	 * Creates an CreateEdgeEvent with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public CreateEdgeEvent(EventManager manager, EventTime time, String contextExpr) {
		super(manager,time,contextExpr);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeCreateEdgeEvents().add(this);
		}else{
			manager.getAfterCreateEdgeEvents().add(this);
		}
	}
	
	
	
}
