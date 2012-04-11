package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.schema.GraphElementClass;

public interface TemporaryGraphElement<SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>> extends GraphElement<SC, IC> {

	public IC convertToRealGraphElement(SC schemaClass);
	
	public void deleteAttribute(String name);
	
}
