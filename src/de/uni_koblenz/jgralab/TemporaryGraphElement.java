package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.schema.GraphElementClass;

public interface TemporaryGraphElement<SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>>
		extends GraphElement<SC, IC> {

	/**
	 * @return the preliminary type of this {@link TemporaryGraphElement}
	 */
	public SC getPreliminaryType();
	
	/**
	 * Sets the preliminary type of this {@link TemporaryGraphElement}
	 * 
	 * @param ec
	 * 			{@link GraphElementClass} representing the new preliminary 
	 * 			type of this {@link TemporaryEdge}
	 */
	public void setPreliminaryType(SC ec);

	public void deleteAttribute(String name);

}
