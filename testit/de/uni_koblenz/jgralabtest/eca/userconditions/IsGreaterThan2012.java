package de.uni_koblenz.jgralabtest.eca.userconditions;

import de.uni_koblenz.jgralab.eca.Condition;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEvent;
import de.uni_koblenz.jgralab.eca.events.Event;

public class IsGreaterThan2012 implements Condition {

	@Override
	public boolean evaluate(Event event) {
		ChangeAttributeEvent cae = (ChangeAttributeEvent) event;
		if((Integer)cae.getNewValue() > 2012)
			return true;
		return false;
	}

}
