package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.EventManager;
import de.uni_koblenz.jgralab.schema.Attribute;

public class ChangeAttributeEvent extends Event {

	public ChangeAttributeEvent(EventManager manager, EventTime time, Class <? extends AttributedElement> type, Attribute at) {
		super(manager,time,type);
		this.concernedAttribute = at;
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeChangeAttributeEvents().add(this);
		}else{
			manager.getAfterChangeAttributeEvents().add(this);
		}		
	}
	
	public ChangeAttributeEvent(EventManager manager, EventTime time, String contextExpr, Attribute at) {
		super(manager,time,contextExpr);
		this.concernedAttribute = at;
		if(time.equals(EventTime.BEFORE)){
			manager.getBeforeChangeAttributeEvents().add(this);
		}else{
			manager.getAfterChangeAttributeEvents().add(this);
		}		
	}

	private Attribute concernedAttribute;

	public Attribute getConcernedAttribute() {
		return concernedAttribute;
	}

	
	
}
