package de.uni_koblenz.jgralab.schema;

public interface CollectionDomain extends CompositeDomain {

	/**
	 * @return the base domain of the collection
	 */
	public Domain getBaseDomain();
}
