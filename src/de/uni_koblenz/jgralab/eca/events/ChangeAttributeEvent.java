package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.EventManager;

public class ChangeAttributeEvent extends Event {

	public ChangeAttributeEvent(EventManager manager, EventTime time, Class <? extends AttributedElement> type, String at) {
		super(manager,time,type);
		this.concernedAttribute = at;
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeChangeAttributeEvents().add(this);
		}else{
			manager.getAfterChangeAttributeEvents().add(this);
		}		
	}
	
	public ChangeAttributeEvent(EventManager manager, EventTime time, String contextExpr, String at) {
		super(manager,time,contextExpr);
		this.concernedAttribute = at;
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeChangeAttributeEvents().add(this);
		}else{
			manager.getAfterChangeAttributeEvents().add(this);
		}		
	}

	public void fire(AttributedElement element, String attributeName){
		if(concernedAttribute.equals(attributeName)){
			this.fire(element);
		}
		
	}
	
	private String concernedAttribute;

	public String getConcernedAttribute() {
		return concernedAttribute;
	}

	
	
}
