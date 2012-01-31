package de.uni_koblenz.jgralabtest.eca.useractions;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEvent;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class RevertEdgeChangingOnHighesLevelAction implements Action<EdgeClass> {
	@Override
	public void doAction(Event<EdgeClass> event) {
		if (event.getNestedCalls() > 1) {
			return;
		}
		if (event instanceof ChangeEdgeEvent) {
			ChangeEdgeEvent cee = (ChangeEdgeEvent) event;
			Edge edge = (cee.getElement());
			if (edge.getAlpha().equals(cee.getNewVertex())) {
				System.out.println("ECA Test Action: Revert changed Edge. "
						+ "Reset Alpha Vertex of Edge \"" + cee.getElement()
						+ "\" from \"" + cee.getNewVertex() + "\" to \""
						+ cee.getOldVertex() + "\"");
				edge.setAlpha(cee.getOldVertex());
			} else if (edge.getOmega().equals(cee.getNewVertex())) {
				System.out.println("ECA Test Action: Revert changed Edge. "
						+ "Reset Omega Vertex of Edge \"" + cee.getElement()
						+ "\" from \"" + cee.getNewVertex() + "\" to \""
						+ cee.getOldVertex() + "\"");
				edge.setOmega(cee.getOldVertex());
			}
		}
	}
}
