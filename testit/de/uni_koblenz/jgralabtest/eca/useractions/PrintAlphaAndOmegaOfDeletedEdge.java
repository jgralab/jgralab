package de.uni_koblenz.jgralabtest.eca.useractions;

import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.DeleteEdgeEvent;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class PrintAlphaAndOmegaOfDeletedEdge implements Action<EdgeClass> {

	@Override
	public void doAction(Event<EdgeClass> event) {
		if(event instanceof DeleteEdgeEvent){
			System.out.println("Edge deleted from "+ ((DeleteEdgeEvent)event).getAlpha() + " to "+ ((DeleteEdgeEvent)event).getOmega()+".");
		}
	}
}
