package de.uni_koblenz.jgralabtest.eca.useractions;

import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription;

public class PrintNewAndOldAttributeValueAction extends Action {

	@Override
	public void doAction() {
		EventDescription e = this.getRule().getEvent();
		if (e instanceof ChangeAttributeEventDescription) {
			ChangeAttributeEventDescription cae = (ChangeAttributeEventDescription) e;
			if (cae.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
