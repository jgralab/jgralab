package de.uni_koblenz.jgralabtest.eca.useractions;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEvent;
import de.uni_koblenz.jgralab.eca.events.Event;

public class CreateAVertexOfSameTypeAction implements Action {

	@Override
	public void doAction(Event ev) {

		if (ev instanceof CreateVertexEvent) {
			if (((CreateVertexEvent) ev).getVertex() != null) {
				System.out
						.println("ECA Test Action: Create a new Vertex of Type: "
								+ ((CreateVertexEvent) ev).getVertex()
										.getM1Class().getName());
				ev.getGraph().createVertex(
								(Class<? extends Vertex>) ((CreateVertexEvent) ev)
										.getVertex().getM1Class());
			}
		}
	}

}
