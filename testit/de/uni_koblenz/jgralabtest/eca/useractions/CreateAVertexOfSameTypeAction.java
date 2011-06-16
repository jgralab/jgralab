package de.uni_koblenz.jgralabtest.eca.useractions;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEvent;
import de.uni_koblenz.jgralab.eca.events.Event;

public class CreateAVertexOfSameTypeAction extends Action {

	@Override
	public void doAction() {
		Event ev = this.getRule().getEvent();
		if (ev instanceof CreateVertexEvent) {
			if (ev.getContext().equals(Event.Context.TYPE)) {
				System.out
						.println("ECA Test Action: Create a new Vertex of Type: "
								+ ev.getType().getName());
				this.getRule().getECARuleManager().getGraph()
						.createVertex((Class<? extends Vertex>) ev.getType());
			}
		}
	}

}
