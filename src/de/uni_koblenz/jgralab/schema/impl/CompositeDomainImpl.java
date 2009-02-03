/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.uni_koblenz.jgralab.schema.BasicDomain;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;

public abstract class CompositeDomainImpl extends DomainImpl implements
		CompositeDomain {

	public CompositeDomainImpl(Schema schema, QualifiedName qn) {
		super(schema, qn);
	}

	/**
	 * Checks if <code>baseDomain</code> is unequal null and is part of
	 * <code>schema</code>.<br>
	 * <br>
	 * <b>pattern:</b> c.isDomainOfSchema(schema1, baseDomain1)<br>
	 * <b>pre:</b> true<br>
	 * <b>post:</b> true iff schema!=baseDomain.getSchema()<br>
	 * <b>post:</b> false otherwise<br>
	 * <b>post:</b> schema1'.equals(schema1) && baseDomain1'.equals(baseDomain1)
	 * 
	 * @param schema
	 *            the schema in which <code>baseDomain</code> must be
	 * @param baseDomain
	 *            the baseDomain
	 * @return true iff <code>baseDomain</code> is part of <code>schema</code><br>
	 *         false otherwise
	 */
	protected boolean isDomainOfSchema(Schema schema, Domain baseDomain) {
		if (schema != baseDomain.getSchema()) {
			return false;
		}
		return true;
	}

	public boolean isComposite() {
		return true;
	}

	public Set<CompositeDomain> getAllComponentCompositeDomains() {
		Domain d;
		HashSet<CompositeDomain> componentCompositeDomains = new HashSet<CompositeDomain>();
		Set<Domain> componentDomains = getAllComponentDomains();

		for (Iterator<Domain> cdit = componentDomains.iterator(); cdit
				.hasNext();) {
			d = cdit.next();
			if (d instanceof BasicDomain)
				cdit.remove();
			else
				componentCompositeDomains.add((CompositeDomain) d);
		}

		return componentCompositeDomains;
	}

	@Override
	public abstract boolean equals(Object o);
}
