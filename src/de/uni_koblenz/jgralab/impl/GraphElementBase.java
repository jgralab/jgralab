package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Attribute;

public interface GraphElementBase extends GraphElement {

	/**
	 * sets the id field of this graph element
	 * 
	 * @param id
	 *            an id
	 */
	public void setId(int id);

	/**
	 * 
	 * @param attr
	 * @throws GraphIOException
	 */
	public void internalSetDefaultValue(Attribute attr) throws GraphIOException;

	/**
	 * Changes the graph version of the graph this element belongs to. Should be
	 * called whenever the graph is changed, all changes like adding, creating
	 * and reordering of edges and vertices or changes of attributes of the
	 * graph, an edge or a vertex are treated as a change.
	 */
	public void graphModified();
}
