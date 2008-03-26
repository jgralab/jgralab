/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
 * @author riediger
 */
public interface Package extends NamedElement {
	/**
	 * Returns thr grUML Schema of this Package.
	 */
	public Schema getSchema();

	/**
	 * Checks if this Package is the default Package.
	 * 
	 * @return true iff this Package is the default Package.
	 */
	public boolean isDefaultPackage();

	/**
	 * Returns the parent Package of this Package.
	 * 
	 * @return the parent Package, or <code>null</code> if this Package is the
	 *         default Package.
	 */
	public Package getParentPackage();

	/**
	 * Returns all subpackages of this Package.
	 * 
	 * @return a Map containing all subpackages of this Package, mapped to their
	 *         simple names.
	 */
	public Map<String, Package> getSubPackages();

	/**
	 * Checks if this Package directly contains a subpackge with the specified
	 * <code>simpleName</code>.
	 * 
	 * @param simpleName
	 *            the name of the subpackage
	 * @return true iff this package contains the specified subpackage.
	 */
	public boolean containsSubPackage(String simpleName);

	/**
	 * Creates a new subpackage with the specified <code>simpleName</code> in
	 * this Package.
	 * 
	 * @param simpleName
	 *            the name of the new subpackage
	 * @return the new subpackage.
	 */
	public Package createSubPackage(String simpleName);

	/**
	 * Returns the subpackage with the specified </code>simpleName</code>.
	 * 
	 * @param simpleName
	 *            the name of the subpackage
	 * @return the subpackage, or <code>null</code> if no such package exists.
	 */
	public Package getSubPackage(String simpleName);

	/**
	 * Returns all Domains of this package.
	 * 
	 * @return a Map containing all Domains of this Package, mapped to their
	 *         simple names.
	 */
	public Map<String, Domain> getDomains();

	/**
	 * Adds the Domain <code>dom</code> to this Package.
	 * 
	 * @param dom
	 *            a Domain
	 */
	public void addDomain(Domain dom);

	/**
	 * Returns all VertexClasses of this package.
	 * 
	 * @return a Map containing all VertexClasses of this Package, mapped to
	 *         their simple names.
	 */
	public Map<String, VertexClass> getVertexClasses();

	/**
	 * Adds the VertexClass <code>vc</code> to this Package.
	 * 
	 * @param vc
	 *            a VertexClass
	 */
	public void addVertexClass(VertexClass vc);

	/**
	 * Returns all VertexClasses of this package.
	 * 
	 * @return a Map containing all EdgeClasses of this Package, mapped to
	 *         their simple names.
	 */
	public Map<String, EdgeClass> getEdgeClasses();

	/**
	 * Adds the EdgeClass <code>ec</code> to this Package.
	 * 
	 * @param ec
	 *            an EdgeClass
	 */
	public void addEdgeClass(EdgeClass ec);
}
