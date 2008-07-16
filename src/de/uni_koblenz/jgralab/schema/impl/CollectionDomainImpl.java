package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;

public abstract class CollectionDomainImpl extends CompositeDomainImpl implements
		CollectionDomain {

	/**
	 * the base domain, every element of an instance of a collection must be of that
	 * domain
	 */
	protected Domain baseDomain;
	
	public CollectionDomainImpl(Schema schema, QualifiedName qn,
			Domain baseDomain) {
		super(schema,qn);
		this.baseDomain = baseDomain;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.CollectionDomain#getBaseDomain()
	 */
	public Domain getBaseDomain() {
		return baseDomain;
	}
}
