package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.schema.EdgeClass;

public interface TemporaryEdge extends Edge,
		TemporaryGraphElement<EdgeClass, Edge> {

		/**
		 * @return the preliminary type of a {@link TemporaryEdge}
		 */
		public EdgeClass getPreliminaryType();
		
		/**
		 * Sets the preliminary type of this {@link TemporaryEdge}
		 * 
		 * @param ec
		 * 			{@link EdgeClass} representing the new preliminary 
		 * 			type of this {@link TemporaryEdge}
		 */
		public void setPreliminaryType(EdgeClass ec);
		
		public Edge bless();
	
}
