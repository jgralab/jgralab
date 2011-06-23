package de.uni_koblenz.jgralabtest.eca.useractions;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription;

public class RevertEdgeChangingAction extends Action {

	@Override
	public void doAction() {
		EventDescription event = this.getRule().getEvent();
		if (event instanceof ChangeEdgeEventDescription) {
			ChangeEdgeEventDescription cee = (ChangeEdgeEventDescription) event;
			Edge edge = (Edge) (cee.getLatestElement());
			if (edge.getAlpha().equals(cee.getLatesNewVertex())) {
				System.out.println("ECA Test Action: Revert changed Edge. "
						+ "Reset Alpha Vertex of Edge \""
						+ cee.getLatestElement() + "\" from \""
						+ cee.getLatesNewVertex() + "\" to \""
						+ cee.getLatestOldVertex() + "\"");
				edge.setAlpha(cee.getLatestOldVertex());
			} else if (edge.getOmega().equals(cee.getLatesNewVertex())) {
				System.out.println("ECA Test Action: Revert changed Edge. "
						+ "Reset Omega Vertex of Edge \""
						+ cee.getLatestElement() + "\" from \""
						+ cee.getLatesNewVertex() + "\" to \""
						+ cee.getLatestOldVertex() + "\"");
				edge.setOmega(cee.getLatestOldVertex());
			}
		}

	}

}
