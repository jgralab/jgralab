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

package de.uni_koblenz.jgralab.schema;

import java.util.Map;

/**
 * The class <code>Package</code> represents a grUML package. A
 * <code>Package</code> can contain <code>Domain</code>s and
 * <code>GraphElementClass</code>es, as well as other <code>Package</code>s.
 * 
 * @author ist@uni-koblenz.de
 */
public interface Package extends NamedElement, Comparable<Package> {

	/*
	 * Touch and die!
	 */
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
	public Map<String, EdgeClass> getEdgeClasses();

	/**
	 * Returns all Domains of this package.
	 * 
	 * @return a Map containing all Domains of this Package, mapped to their
	 *         simple names.
	 */
	public Map<String, Domain> getDomains();

	public Map<String, GraphClass> getGraphClasses();

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
	public Map<String, Package> getSubPackages();

	/**
	 * Returns all VertexClasses of this package.
	 * 
	 * @return a Map containing all VertexClasses of this Package, mapped to
	 *         their simple names.
	 */
	public Map<String, VertexClass> getVertexClasses();

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
}
