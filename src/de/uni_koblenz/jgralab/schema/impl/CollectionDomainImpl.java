package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.exception.WrongSchemaException;

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

		if (pkg.getSchema().getDomain(baseDomain.getQualifiedName()) != baseDomain) {
			throw new WrongSchemaException(baseDomain.getQualifiedName()
					+ " must be a domain of the schema "
					+ pkg.getSchema().getQualifiedName());
		}

		this.baseDomain = baseDomain;
	}

	@Override
	public Domain getBaseDomain() {
		return baseDomain;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CollectionDomain) {
			CollectionDomain other = (CollectionDomain) o;
			if (!getSchema().getQualifiedName().equals(
					other.getSchema().getQualifiedName())) {
				return false;
			}
			// This may happen with TGSchema2Java while loading a Schema
			// from a file...
			if ((baseDomain == null) || (other.getBaseDomain() == null)) {
				return false;
			}
			return (getClass() == other.getClass())
					&& baseDomain.equals(other.getBaseDomain());
		}
		return false;
	}
}
