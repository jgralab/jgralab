/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
