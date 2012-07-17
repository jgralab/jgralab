package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.schema.EdgeClass;

public interface TemporaryEdge extends Edge,
		TemporaryGraphElement<EdgeClass, Edge> {

		public EdgeClass getPreliminaryType();
		
		public void setPreliminaryType(EdgeClass ec);
	
}
