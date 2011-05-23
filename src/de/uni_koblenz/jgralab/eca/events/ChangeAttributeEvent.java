package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.eca.EventManager;
import de.uni_koblenz.jgralab.schema.Attribute;

public class ChangeAttributeEvent extends Event {

	public ChangeAttributeEvent(EventManager manager, EventTime time) {
		super(manager,time);
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

	public void setConcernedAttribute(Attribute concernedAttribute) {
		this.concernedAttribute = concernedAttribute;
	}
	
}
