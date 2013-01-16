/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public abstract class CollectionDomainImpl extends CompositeDomainImpl
		implements CollectionDomain {

	/**
	 * The base domain, every element of an instance of a collection must be of
	 * that domain
	 */
	protected Domain baseDomain;

	protected CollectionDomainImpl(String simpleName, PackageImpl pkg,
			Domain baseDomain) {
		super(simpleName, pkg);

		if (schema != ((DomainImpl) baseDomain).schema) {
			throw new SchemaException("Base domain "
					+ baseDomain.getQualifiedName()
					+ " belongs to a different schema.");
		}

		this.baseDomain = baseDomain;
		schema.addDomainDependency(this, baseDomain);
	}

	@Override
	public Domain getBaseDomain() {
		return baseDomain;
	}

	@Override
	protected void registerAttribute(Attribute a) {
		attributes = attributes.plus(a);
		((DomainImpl) baseDomain).registerAttribute(a);
	}
}
