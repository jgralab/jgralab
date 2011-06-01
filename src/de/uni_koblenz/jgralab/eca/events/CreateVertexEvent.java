package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.EventManager;

public class CreateVertexEvent extends Event {

	/**
	 * Creates an CreateVertexEvent with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public CreateVertexEvent(EventManager manager, EventTime time, Class <? extends AttributedElement> type) {
		super(manager, time,type);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeCreateVertexEvents().add(this);
		}else{
			manager.getAfterCreateVertexEvents().add(this);
		}
	}

	/**
	 * Creates an CreateVertexEvent with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public CreateVertexEvent(EventManager manager, EventTime time, String contextExpr) {
		super(manager, time,contextExpr);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeCreateVertexEvents().add(this);
		}else{
			manager.getAfterCreateVertexEvents().add(this);
		}
	}

	
	
}
