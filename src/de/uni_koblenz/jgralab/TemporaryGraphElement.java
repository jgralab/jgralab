package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.schema.GraphElementClass;

public interface TemporaryGraphElement<SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>>
		extends GraphElement<SC, IC> {

	/**
	 * Returns a new graph element of <code>schemaClass</code> that takes the
	 * place of this temporary element.
	 *
	 * @param schemaClass
	 * @return a new graph element of <code>schemaClass</code> that takes the
	 *         place of this temporary element.
	 */
	public IC bless(SC schemaClass);

	public void deleteAttribute(String name);

}
