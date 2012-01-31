package de.uni_koblenz.jgralabtest.eca.useractions;

import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEvent;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateAVertexOfSameTypeAction implements Action<VertexClass> {
	@Override
	public void doAction(Event<VertexClass> ev) {
		if (ev instanceof CreateVertexEvent) {
			System.out.println("ECA Test Action: Create a new Vertex of Type: "
					+ ev.getType().getQualifiedName());
			ev.getGraph().createVertex(ev.getType());

		}
	}

}
