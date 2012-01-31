package de.uni_koblenz.jgralabtest.eca.userconditions;

import de.uni_koblenz.jgralab.eca.Condition;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEvent;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class IsGreaterThan2012<AEC extends AttributedElementClass<AEC, ?>>
		implements Condition<AEC> {

	@Override
	public boolean evaluate(Event<AEC> event) {
		ChangeAttributeEvent<AEC> cae = (ChangeAttributeEvent<AEC>) event;
		if ((Integer) cae.getNewValue() > 2012) {
			return true;
		}
		return false;
	}

}
