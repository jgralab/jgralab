/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.schema.BasicDomain;
import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public abstract class NamedElementImpl implements NamedElement {

	protected final SchemaImpl schema;

	/**
	 * The package containing this named element. <code>null</code> if this
	 * named element is the <code>DefaultPackage</code>.
	 */
	protected PackageImpl parentPackage;

	/**
	 * The fully qualified name of an element in a schema.<br />
	 * If this named element is the <code>DefaultPackage</code> or this named
	 * element lies directly in the <code>DefaultPackage</code>, then the
	 * qualified name equals this named element's simple name.<br />
	 * Else it is composed of this named element's {@link #parentPackage
	 * parentPackage} name, and {@link #simpleName simpleName}, seperated by a
	 * '.' character. <br/>
	 * <code>qualifiedName = packageName + "." + simpleName</code>
	 */
	protected String qualifiedName;

	/**
	 * Unique name of an element in a package without the qualified package
	 * name.
	 */
	protected String simpleName;

	/**
	 * Pattern to match the simple name of Collection-/Map-Domain elements with.<br />
	 * Check the preconditions section
	 * {@link #NamedElementImpl(String, Package, Schema) here} for details.
	 */
	private static final Pattern COLLECTION_OR_MAPDOMAIN_NAME_PATTERN = Pattern
			.compile("[.]?\\p{Upper}\\w*<[<>., _\\w]+>$");

	/**
	 * Pattern to match the simple name of Package elements with.<br />
	 * Check the preconditions section
	 * {@link #NamedElementImpl(String, Package, Schema) here} for details.
	 */
	static final Pattern PACKAGE_NAME_PATTERN = Pattern
			.compile("\\p{Lower}\\w*");

	/**
	 * Pattern to match the simple name of AttributedElementClass and any Domain
	 * other then Collection-Domain elements with.<br />
	 * Check the preconditions section
	 * {@link #NamedElementImpl(String, Package, Schema) here} for details.
	 */
	static final Pattern ATTRELEM_OR_NOCOLLDOMAIN_PATTERN = Pattern
			.compile("\\p{Upper}\\w*?");

	/**
	 * The list of comments of this NamedElement. Only contains non-empty
	 * Strings. If no comment exists, <code>comments</code> is null.
	 */
	protected List<String> comments;

	/**
	 * Creates a new named element with the specified name in the given parent
	 * package and schema.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>namedElement = new NamedElementImpl(sn, pkg, schema);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>simpleName:
	 * <ul>
	 * <li>The simple name is not empty, except if this named element is the
	 * <code>DefaultPackage</code> and the
	 * {@link de.uni_koblenz.jgralab3.schema.Package.DEFAULTPACKAGE_NAME
	 * DEFAULTPACKAGE_NAME} is the empty string. In that case, the parent
	 * package must be <code>null</code>.</li>
	 * <li>The simple name starts with a letter.</li>
	 * <li>The following characters in the simple name are either alphanumeric
	 * or the '_' symbol. List-/Map-/Set-Domain may also contain '<>.,'
	 * characters.</li>
	 * <li>The simple name ends with an alphanumeric character, or in the case
	 * of a List-/Map-/Set-Domain with a '>' character.</li>
	 * <li>The qualified name, made of the package name and the simple name,
	 * must differ from any other element´s name in the schema.</li>
	 * <li>The simple name of Package-instances starts with a small letter.</li>
	 * <li>The simple name of Domain-/AttributedElementClass-instances starts
	 * with a capital letter.</li>
	 * </ul>
	 * </li>
	 * <li>pkg:
	 * <ul>
	 * <li>The parent package is not <code>null</code>, except if the named
	 * element is the <code>DefaultPackage</code>.</li>
	 * <li>The specified parent package for any Basic-/Collection-/Map-Domain-
	 * and/or GraphClass-element, must be the <code>DefaultPackage</code>.</li>
	 * </ul>
	 * </li>
	 * <li>schema:
	 * <ul>
	 * <li>Each and every named element must be contained in a schema. Therefore
	 * schema must not be <code>null</code>.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>namedElement.package</code> has one the following values:
	 * <ul>
	 * <li><code>null</code> if <code>namedElement</code> is the
	 * <code>DefaultPackage</code></li>
	 * <li>a valid (not <code>null</code>) parent package, in any other case</li>
	 * </ul>
	 * </li>
	 * <li><code>namedElement.qualifiedName</code> has one of the following
	 * values:
	 * <ul>
	 * <li>if <code>namedElement</code> is the <code>DefaultPackage</code>, then
	 * it equals the <code>simpleName</code></li>
	 * <li>in any other case, it is a composition of the
	 * <code>packageName</code>, a '.' character and the <code>simpleName</code>
	 * .<br />
	 * Formally: <code>qualifiedName = packageName + "." + simpleName</li>
	 * </ul>
	 * </li>
	 * <li><code>namedElement.schema</code> is the given schema containing this
	 * named element</li>
	 * <li><code>namedElement.simpleName</code> has one of the following values:
	 * <ul>
	 * <li>it equals
	 * {@link de.uni_koblenz.jgralab3.schema.Package.DEFAULTPACKAGE_NAME
	 * DEFAULTPACKAGE_NAME} if <code>namedElement</code> represents the
	 * <code>DefaultPackage</code></li>
	 * <li>any other valid value</li>
	 * </ul>
	 * </li>
	 * <li><code>namedElement.uniqueName</code> has one of the following values:
	 * <ul>
	 * <li>it equals <code>namedElement.simpleName</code> if there is no other
	 * named element with this <code>simpleName</code> in the containing schema</li>
	 * <li>it equals the composition of the package name and
	 * <code>simpleName</code></li>
	 * </ul>
	 * In either case, all '.' characters are replaced by '$' characters.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param simpleName
	 *            this named element's simple name
	 * @param pkg
	 *            the package containing this named element
	 * @param schema
	 *            the schema containing this named element
	 * @throws IllegalArgumentException
	 *             if:
	 *             <ul>
	 *             <li>the simple name does not meet the required format (see
	 *             preconditions)</li>
	 *             <li>the simple name is a reserved Java word</li>
	 *             </ul>
	 * @throws SchemaException
	 *             if:
	 *             <ul>
	 *             <li>no schema was specified</li>
	 *             <li>the package is
	 *             <code>null</null> for any other element then the <code>DefaultPackage</code>
	 *             </li>
	 *             <li>the element is a
	 *             {@link de.uni_koblenz.jgralab.schema.BasicDomain BasicDomain}
	 *             / {@link de.uni_koblenz.jgralab.schema.CollectionDomain
	 *             CollectionDomain} /
	 *             {@link de.uni_koblenz.jgralab.schema.MapDomain MapDomain} or
	 *             a {@link de.uni_koblenz.jgralab.schema.GraphClass GraphClass}
	 *             and the parent package is not the <code>DefaultPackage</code>
	 *             </li>
	 *             <li>there already is an element in the containing schema,
	 *             that has the exact same qualified name</li>
	 *             </ul>
	 */
	protected NamedElementImpl(String simpleName, PackageImpl pkg,
			SchemaImpl schema) {
		/*
		 * Every named element must be contained in a schema.
		 */
		if (schema == null) {
			throw new SchemaException("Cannot create the element '"
					+ simpleName
					+ "' because no containing schema was specified.");
		}

		this.schema = schema;

		/*
		 * An empty (null) parent package is only allowed for the
		 * DefaultPackage.
		 */
		if (pkg == null) {
			/*
			 * The DefaultPackage must have the predefined standard simple name,
			 * and the schema in which it is created must not already contain a
			 * DefaultPackage.
			 */
			if (simpleName.equals(Package.DEFAULTPACKAGE_NAME)
					&& (this instanceof PackageImpl)
					&& (schema.getDefaultPackage() == null)) {
				this.qualifiedName = Package.DEFAULTPACKAGE_NAME;
				this.parentPackage = null;
				this.simpleName = Package.DEFAULTPACKAGE_NAME;
				comments = new ArrayList<>();
				return;
			} else {
				throw new SchemaException("Cannot create the element '"
						+ simpleName + "' cause no parent package was given.");

			}
		}
		this.parentPackage = pkg;

		/*
		 * The simple name must not be empty (except for the DefaultPackage).
		 * The simple name must start with a letter (expect for
		 * Map-/Set-/List-/Collection-Domains which may start with a '.'). Any
		 * following character must be alphanumeric and/or a '_' character
		 * (Composite-/EnumDomain simple names may also have '.<>,' characters).
		 * The simple name must end with an alphanumeric character.
		 * 
		 * Simple names of Domains & AttributedElements start with a capital
		 * letter, whereas the simple name for a Package starts with a small
		 * letter.
		 */
		if ((this instanceof CollectionDomain) || (this instanceof MapDomain)) {
			if (!COLLECTION_OR_MAPDOMAIN_NAME_PATTERN.matcher(simpleName)
					.matches()) {
				throw new SchemaException(
						"Invalid simpleName for Collection- or MapDomain '"
								+ simpleName
								+ "': The simple name must not be empty. "
								+ "The simple name must start with a uppercase letter. "
								+ "Any following character must be alphanumeric or a '_' character (List-/Map-/Set-Domain simple names may also have '.<>,' characters).");
			}
		} else if (this instanceof Package) {
			if (!PACKAGE_NAME_PATTERN.matcher(simpleName).matches()) {
				throw new SchemaException(
						"Invalid simpleName for Package '"
								+ simpleName
								+ "': The simple name must start with a small letter. "
								+ "Any following character must be alphanumeric and/or a '_' character. "
								+ "The simple name must end with an alphanumeric character.");
			}
		} else if (!ATTRELEM_OR_NOCOLLDOMAIN_PATTERN.matcher(simpleName)
				.matches()) {
			throw new SchemaException(
					"Invalid simpleName for AttributedElementClass or Domain '"
							+ simpleName
							+ "': The simple name must not be empty. "
							+ "The simple name must start with a letter. "
							+ "Any following character must be alphanumeric and/or a '_' character. "
							+ "The simple name must end with an alphanumeric character.");
		}

		/*
		 * Words that are reserved by Java itself are not allowed as element
		 * names.
		 */
		if (Schema.RESERVED_JAVA_WORDS.contains(simpleName)) {
			throw new SchemaException("Invalid simpleName '" + simpleName
					+ "': The simple name must not be a reserved Java word.");
		}

		this.simpleName = simpleName;

		/*
		 * The qualifiedName is made of: packageName + "." + simpleName In the
		 * event that this element is directly contained in the DefaultPackage,
		 * the qualifiedName equals the simpleName.
		 */
		qualifiedName = (pkg.getQualifiedName().equals(
				Package.DEFAULTPACKAGE_NAME) ? "" : pkg.getQualifiedName()
				+ ".")
				+ simpleName;

		/*
		 * The package for Basic-/Map-/Collection-Domains (List, Set) and
		 * GraphClass must be the DefaultPackage.
		 */
		if ((this instanceof BasicDomain) || (this instanceof CollectionDomain)
				|| (this instanceof MapDomain) || (this instanceof GraphClass)) {
			if (!parentPackage.isDefaultPackage()) {
				throw new SchemaException(
						"Invalid parent package '"
								+ pkg.getQualifiedName()
								+ "'.\n"
								+ "The parent package for BasicDomains (Boolean, Double, Integer, Long, String),\n"
								+ "CollectionDomains (List, Set) and GraphClasses must be the DefaultPackage.");
			}
		}

		/*
		 * Check if there already is a named element with the same qualified
		 * name in the schema.
		 */
		if (schema.knows(qualifiedName)) {
			throw new SchemaException(
					"The Schema already contains a named element with qualified name '"
							+ qualifiedName + "'.");
		}

		schema.addNamedElement(this);
		comments = new ArrayList<>();
	}

	@Override
	public String toString() {
		return getQualifiedName();
	}

	@Override
	public int compareTo(NamedElement other) {
		return this.qualifiedName.compareTo(other.getQualifiedName());
	}

	@Override
	public final String getFileName() {
		return qualifiedName.replace('.', File.separatorChar);
	}

	@Override
	public final Package getPackage() {
		return parentPackage;
	}

	@Override
	public final String getPackageName() {
		return parentPackage != null ? parentPackage.getQualifiedName() : null;
	}

	@Override
	public final String getPathName() {
		return parentPackage != null ? getPackageName().replace('.',
				File.separatorChar) : null;
	}

	@Override
	public final String getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public final String getQualifiedName(Package pkg) {
		if (parentPackage == pkg) {
			return simpleName;
		} else if (parentPackage.isDefaultPackage()) {
			return Package.DEFAULTPACKAGE_NAME + "." + simpleName;
		} else {
			return qualifiedName;
		}
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public final String getSimpleName() {
		return simpleName;
	}

	@Override
	public String getUniqueName() {
		for (NamedElement n : schema.namedElements.values()) {
			if (n.getSimpleName().equals(simpleName) && (n != this)) {
				return toUniqueNameNotation(qualifiedName);
			}
		}
		return simpleName;
	}

	@Override
	public final int hashCode() {
		int hash = 113 + schema.hashCode();
		hash = 23 * hash + qualifiedName.hashCode();
		return hash;
	}

	@Override
	public final boolean equals(Object o) {
		if ((o == null) || !(o instanceof NamedElement)) {
			return false;
		}
		NamedElementImpl other = (NamedElementImpl) o;
		return schema.equals(other.schema)
				&& qualifiedName.equals(other.qualifiedName);
	}

	/**
	 * Transforms a qualified name into unique name notation. This is achieved
	 * by replacing every occurrence of a <code>'.'</code> characters in the
	 * given qualified name by a <code>'$'</code> character.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>un = NamedElementImpl.toUniqueName(qn);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>un</code> equals <code>qn</code>, except
	 * that every occurrence of a '.' character has been replaced by a '$'
	 * character. As no named element allows for '$' characters in it's
	 * qualified name, there is no problem here.
	 * </p>
	 * 
	 * @param qualifiedName
	 *            the qualified name to convert to unique name notation
	 * 
	 * @return the unique name derived from a given qualified name
	 */
	public static String toUniqueNameNotation(String qualifiedName) {
		return qualifiedName.replace('.', '$');
	}

	@Override
	public List<String> getComments() {
		return Collections.unmodifiableList(comments);
	}

	@Override
	public void addComment(String comment) {
		schema.assertNotFinished();
		if (comment == null) {
			return;
		}
		comment = comment.trim();
		if (comment.length() > 0) {
			if (this.comments == null) {
				this.comments = new ArrayList<>();
			}
			this.comments.add(comment);
		}
	}

	@Override
	public void setQualifiedName(String newQName) {
		throw new UnsupportedOperationException("Renaming not allowed for "
				+ getClass().getName());
	}

	/**
	 * Registers this element's qualified name with the schema (and graph class
	 * for graph element classes), and the simple name with the containing
	 * package.
	 */
	protected void register() {
		schema.namedElements.put(qualifiedName, this);
	}

	/**
	 * Removes this element's qualified name from the schema's namedElements map
	 * (and the graph class vertexClasses/edgeClasses maps for graph element
	 * classes), and also removes this element's simple name from the containing
	 * package's maps.
	 */
	protected void unregister() {
		schema.namedElements.remove(qualifiedName);
	}
}
