package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.EventManager;

public class DeleteVertexEvent extends Event {

	/**
	 * Creates an DeleteVertexEvent with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public DeleteVertexEvent(EventManager manager, EventTime time, Class <? extends AttributedElement> type) {
		super(manager,time,type);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeDeleteVertexEvents().add(this);
		}else{
			manager.getAfterDeleteVertexEvents().add(this);
		}
	}

	/**
	 * Creates an DeleteVertexEvent with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public DeleteVertexEvent(EventManager manager, EventTime time, String contextExpr) {
		super(manager,time,contextExpr);
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeDeleteVertexEvents().add(this);
		}else{
			manager.getAfterDeleteVertexEvents().add(this);
		}
	}

	
}
