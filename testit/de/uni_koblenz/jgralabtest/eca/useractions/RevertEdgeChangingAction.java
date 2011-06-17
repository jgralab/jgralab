package de.uni_koblenz.jgralabtest.eca.useractions;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEvent;
import de.uni_koblenz.jgralab.eca.events.Event;

public class RevertEdgeChangingAction extends Action {

	@Override
	public void doAction() {
		Event event = this.getRule().getEvent();
		if (event instanceof ChangeEdgeEvent) {
			ChangeEdgeEvent cee = (ChangeEdgeEvent) event;
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
