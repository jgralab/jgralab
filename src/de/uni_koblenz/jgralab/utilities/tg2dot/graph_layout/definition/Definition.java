package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * A Definition holds attributes defining the style of one set of elements. The
 * set of elements consists of either elements of type {@link Vertex} or
 * {@link Edge}.
 * 
 * @author ist@uni-koblenz.de
 */
public interface Definition {

	/**
	 * Returns to a given attribute name its associated attribute GReQl-query as
	 * string.
	 * 
	 * @param name
	 *            Name of the attribute.
	 * @return Value of the attribute.
	 */
	public String getAttributeValue(String name);

	/**
	 * Sets a attribute with the given name and value. This method will
	 * overwrite any pre-existing attribute with the same attribute name.
	 * 
	 * @param name
	 *            Name of the attribute.
	 * @param value
	 *            Value of the attribute.
	 */
	public void setAttribute(String name, String value);

	/**
	 * Returns a set of attribute names defined by this Definition.
	 * 
	 * @return Set of attribute names.
	 */
	public Set<String> getAttributeNames();

	/**
	 * Overwrites every attribute of this definitions with all attributes of the
	 * given Definition.
	 * 
	 * @param definition
	 *            Definition with additional or even more important attributes.
	 */
	public void overwriteAttributes(Definition definition);

	/**
	 * Adds only non existing attributes from the given definition to his
	 * definition.
	 * 
	 * @param definition
	 *            Definition with additional but less important attributes.
	 */
	public void addNonExistingAttributes(Definition definition);

	/**
	 * Will return a clone of the current definition. All data structures are
	 * clones but not their stores elements are only referenced.
	 * 
	 * @return A cloned definition.
	 */
	public Definition clone();
}
