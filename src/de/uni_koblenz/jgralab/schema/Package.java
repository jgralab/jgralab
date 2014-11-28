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

package de.uni_koblenz.jgralab.schema;

import org.pcollections.PSet;

/**
 * The class <code>Package</code> represents a grUML package. A
 * <code>Package</code> can contain <code>Domain</code>s and
 * <code>GraphElementClass</code>es, as well as other <code>Package</code>s.
 *
 * @author ist@uni-koblenz.de
 */
public interface Package extends NamedElement {

	/**
	 * The name of the <code>DefaultPackage</code>.
	 */
	public static final String DEFAULTPACKAGE_NAME = "";

	/**
	 * Returns all EdgeClasses of this package.
	 *
	 * @return a Map containing all EdgeClasses of this Package, mapped to their
	 *         simple names.
	 */
	public PSet<EdgeClass> getEdgeClasses();

	public EdgeClass getEdgeClass(String simpleName);

	/**
	 * Returns all Domains of this package.
	 *
	 * @return a Map containing all Domains of this Package, mapped to their
	 *         simple names.
	 */
	public PSet<Domain> getDomains();

	public Domain getDomain(String simpleName);

	/**
	 * Retrieves the subpackage with the given simple name.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>p = package.getSubPackage(sn);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none<br/>
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> p takes one of the following values:
	 * <ul>
	 * <li><code>null</code> if no subpackage with the given simple name was
	 * found in this package</li>
	 * <li>the package in the other case</code>
	 * </ul>
	 * </p>
	 *
	 * @param sn
	 *            the simple name of the subpackage to obtain from this package
	 * @return the subpackage matching the simple name in this package,
	 *         <code>null</code> if no match is found
	 */
	public Package getSubPackage(String sn);

	/**
	 * Returns all subpackages of this Package.
	 *
	 * @return a Map containing all subpackages of this Package, mapped to their
	 *         simple names.
	 */
	public PSet<Package> getSubPackages();

	/**
	 * Returns all VertexClasses of this package. Note that the default package
	 * also contains the default vertex class.
	 *
	 * @return a Map containing all VertexClasses of this Package, mapped to
	 *         their simple names.
	 */
	public PSet<VertexClass> getVertexClasses();

	public VertexClass getVertexClass(String simpleName);

	/**
	 * Checks if this package contains a named element with the given simple
	 * name.
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code>containsNE = package.containsNamedElement(sn);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none<br/>
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> <code>containsNE</code> is <code>true</code>, if
	 * this package contains a named element with the given simple name. Else it
	 * is <code>false</code>.
	 * </p>
	 *
	 * @param sn
	 *            the simple name of the named element to look for in this
	 *            package
	 * @return <code>true</code>, if this package contains a named element with
	 *         the given simple name
	 */
	public boolean containsNamedElement(String sn);

	/**
	 * Checks if this Package is the default Package.
	 *
	 * @return true iff this Package is the default Package.
	 */
	public boolean isDefaultPackage();

	/**
	 * Deletes this package. Only empty packages can be deleted. Note that the
	 * default package cannot be deleted.
	 */
	public void delete();
}
