package de.uni_koblenz.jgralabtest.eca.useractions;

import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEvent;
import de.uni_koblenz.jgralab.eca.events.Event;

public class PrintNewAndOldAttributeValueAction extends Action {

	@Override
	public void doAction() {
		Event e = this.getRule().getEvent();
		if (e instanceof ChangeAttributeEvent) {
			ChangeAttributeEvent cae = (ChangeAttributeEvent) e;
			if (cae.getTime().equals(Event.EventTime.BEFORE)) {
				System.out.println("ECA Test Message: " + "Attribute \""
						+ cae.getConcernedAttribute() + "\" of \""
						+ cae.getLatestElement() + "\" will change from \""
						+ cae.getLatesOldValue() + "\" to \""
						+ cae.getLatestNewValue() + "\"");
			} else {
				System.out.println("ECA Test Message: " + "Attribute \""
						+ cae.getConcernedAttribute() + "\" of \""
						+ cae.getLatestElement() + "\" changed from \""
						+ cae.getLatesOldValue() + "\" to \""
						+ cae.getLatestNewValue() + "\"");
			}
		}

	}

}
