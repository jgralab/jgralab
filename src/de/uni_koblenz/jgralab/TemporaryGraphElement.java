package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.schema.GraphElementClass;

public interface TemporaryGraphElement<SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>> extends GraphElement<SC, IC> {

	public SC transformToRealGraphElement();
	
	public void deleteAttribute(String name);
	
}
