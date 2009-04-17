package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Package;

public abstract class CollectionDomainImpl extends CompositeDomainImpl
		implements CollectionDomain {

	/**
	 * The base domain, every element of an instance of a collection must be of
	 * that domain
	 */
	protected Domain baseDomain;

	protected CollectionDomainImpl(String simpleName, Package pkg,
			Domain baseDomain) {
		super(simpleName, pkg);

		assert pkg.getSchema().getDomain(baseDomain.getQualifiedName()) != null : baseDomain
				.getQualifiedName()
				+ " must be a domain of the schema "
				+ pkg.getSchema().getQualifiedName();

		this.baseDomain = baseDomain;
	}

	@Override
	public Domain getBaseDomain() {
		return baseDomain;
	}
}
