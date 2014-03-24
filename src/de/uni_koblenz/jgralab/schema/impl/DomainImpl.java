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

import org.pcollections.ArrayPSet;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public abstract class DomainImpl extends NamedElementImpl implements Domain {

	protected CodeSnippet maybeWrapInUnsetCheck(String graphIo,
			boolean withUnsetCheck, String code) {
		if (withUnsetCheck) {
			return new CodeSnippet(
					"boolean attrIsSet = true;",
					"if (!"
							+ graphIo
							+ ".isNextToken(de.uni_koblenz.jgralab.impl.TgLexer.Token.UNSET_LITERAL)) {",
					"\t" + code, "} else {", "\t" + graphIo + ".match();",
					"\tattrIsSet = false;", "}");
		}
		return new CodeSnippet(code);
	}

	/**
	 * All Attributes that have this domain.
	 */
	PSet<Attribute> attributes = ArrayPSet.<Attribute> empty();

	protected DomainImpl(String simpleName, PackageImpl pkg) {
		super(simpleName, pkg, (SchemaImpl) pkg.getSchema());
		schema.addDomain(this);
		parentPackage.addDomain(this);
	}

	@Override
	public String toString() {
		return "domain " + qualifiedName;
	}

	@Override
	public String getUniqueName() {
		return qualifiedName;
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

	@Override
	protected final void register() {
		super.register();
		schema.domains.put(qualifiedName, this);
		parentPackage.domains.put(simpleName, this);
	}

	@Override
	protected final void unregister() {
		super.unregister();
		schema.domains.remove(qualifiedName);
		parentPackage.domains.remove(simpleName);
	}

	@Override
	public void delete() {
		throw new SchemaException("Cannot delete domain " + qualifiedName);
	}

	@Override
	public PSet<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * Registers the given attribute as user of this domain. Collection, Map,
	 * and RecordDomains override this and also register their base/key/value
	 * domains or their record component domains.
	 * 
	 * That's done for disallowing the deletion of domains that are still used
	 * as attribute domain. E.g., if there's still an List&lt;Map&lt;String,
	 * FooRecord&gt;&gt; attribute, you must not delete the FooRecord domain.
	 */
	protected void registerAttribute(Attribute a) {
		attributes = attributes.plus(a);
	}
}
