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

import java.util.Map;
import java.util.TreeMap;

import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public final class PackageImpl extends NamedElementImpl implements Package {

	private final Map<String, Domain> domains = new TreeMap<String, Domain>();

	private final Map<String, EdgeClass> edgeClasses = new TreeMap<String, EdgeClass>();

	private final Map<String, GraphClass> graphClasses = new TreeMap<String, GraphClass>();

	private final Schema schema;

	private final Map<String, Package> subPackages = new TreeMap<String, Package>();

	private final Map<String, VertexClass> vertexClasses = new TreeMap<String, VertexClass>();

	/**
	 * Creates a new <code>DefaultPackage</code> in the given Schema.
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code>p = PackageImpl.createDefaultPackage(schema);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none<br/>
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> p is the newly created <code>DefaultPackage</code>
	 * for this schema
	 * </p>
	 *
	 * @param schema
	 *            the schema containing the new <code>DefaultPackage</code>
	 * @return the newly created <code>DefaultPackage</code> for the given
	 *         schema
	 * @throws SchemaException
	 *             if the <code>DefaultPackage</code> already exists in the
	 *             given schema
	 */
	static Package createDefaultPackage(Schema schema) {
		assert schema.getDefaultPackage() == null : "DefaultPackage already created!";
		return new PackageImpl(schema);
	}

	/**
	 * Constructor for the default package
	 *
	 * @param schema
	 */
	private PackageImpl(Schema schema) {
		this(Package.DEFAULTPACKAGE_NAME, null, schema);
	}

	PackageImpl(String simpleName, Package parentPackage, Schema schema) {
		super(simpleName, parentPackage, schema);
		this.schema = schema;
		register();
	}

	@Override
	protected void register() {
		if (parentPackage != null) {
			((PackageImpl) parentPackage).addSubPackage(this);
		}
		((SchemaImpl) schema).addPackage(this);
	}

	void addDomain(Domain dom) {
		assert dom.getPackage() == this : "The domain does not belong into this package ("
				+ getQualifiedName()
				+ "). It belongs here: "
				+ dom.getPackageName();
		assert !domains.containsKey(dom.getSimpleName())
				&& !domains.containsValue(dom) : "This package ("
				+ getQualifiedName() + ") already contains a domain called "
				+ dom.getSimpleName();
		domains.put(dom.getSimpleName(), dom);
	}

	/**
	 * Adds the EdgeClass <code>ec</code> to this Package.
	 *
	 * @param ec
	 *            an EdgeClass
	 */
	void addEdgeClass(EdgeClass ec) {
		assert ec.getPackage() == this : "The edge class '"
				+ ec.getQualifiedName()
				+ "' does not belong into the package '" + getQualifiedName()
				+ "' but into '" + ec.getPackageName() + "'.";
		assert !edgeClasses.containsKey(ec.getSimpleName())
				&& !edgeClasses.containsValue(ec) : "This package ("
				+ getQualifiedName()
				+ ") already contains an edge class called "
				+ ec.getSimpleName();
		edgeClasses.put(ec.getSimpleName(), ec);
	}

	/**
	 * Adds the GraphClass gc to this Package. This action is only allowed, if
	 * this package is the DefaultPackage.
	 */
	void addGraphClass(GraphClass gc) {
		if (!isDefaultPackage()) {
			throw new SchemaException(
					"The GraphClass must be situated in the DefaultPackage.");
		}
		assert !graphClasses.containsKey(gc.getSimpleName())
				&& !graphClasses.containsValue(gc) : "This package ("
				+ getQualifiedName()
				+ ") already contains a graph class called "
				+ gc.getSimpleName();

		graphClasses.put(gc.getQualifiedName(), gc);
	}

	/**
	 * Adds the subpackage <code>subPkg</code> to this Package.
	 *
	 * @param subPkg
	 *            a subpackage
	 */
	void addSubPackage(Package subPkg) {
		assert subPkg.getPackage() == this : "The subpackage does not belong into the package '"
				+ getQualifiedName()
				+ "' but into '"
				+ subPkg.getPackageName()
				+ "'.";
		assert !subPackages.containsKey(subPkg.getSimpleName()) : "The package '"
				+ getQualifiedName()
				+ "' already contains a subpackage called '"
				+ subPkg.getSimpleName() + "'.";
		subPackages.put(subPkg.getSimpleName(), subPkg);
	}

	/**
	 * Adds the VertexClass <code>vc</code> to this Package.
	 *
	 * @param vc
	 *            a VertexClass
	 */
	void addVertexClass(VertexClass vc) {
		assert vc != null : "The given VertexClass is null.";
		assert vc.getPackage() == this : "The vertex class '"
				+ vc.getQualifiedName()
				+ "' does not belong into the package '" + getQualifiedName()
				+ "' but into '" + vc.getPackageName() + "'";
		assert !vertexClasses.containsKey(vc.getSimpleName())
				&& !vertexClasses.containsValue(vc) : "This package ("
				+ getQualifiedName()
				+ ") already contains a vertex class called \""
				+ vc.getSimpleName() + "\"";
		vertexClasses.put(vc.getSimpleName(), vc);
	}

	@Override
	public int compareTo(Package other) {
		return this.qualifiedName.compareTo(other.getQualifiedName());
	}

	@Override
	public boolean containsNamedElement(String sn) {
		return domains.containsKey(sn)
				|| edgeClasses.containsKey(sn)
				|| (isDefaultPackage() && schema.getDefaultGraphClass() != null)
				|| vertexClasses.containsKey(sn) || subPackages.containsKey(sn);
	}

	@Override
	public Map<String, Domain> getDomains() {
		return domains;
	}

	@Override
	public Map<String, EdgeClass> getEdgeClasses() {
		return edgeClasses;
	}

	@Override
	public Map<String, GraphClass> getGraphClasses() {
		return graphClasses;
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public Package getSubPackage(String sn) {
		return subPackages.get(sn);
	}

	@Override
	public Map<String, Package> getSubPackages() {
		return subPackages;
	}

	@Override
	public Map<String, VertexClass> getVertexClasses() {
		return vertexClasses;
	}

	@Override
	public boolean isDefaultPackage() {
		return this == schema.getDefaultPackage();
	}

	@Override
	public String toString() {
		return "package " + qualifiedName;
	}
}
