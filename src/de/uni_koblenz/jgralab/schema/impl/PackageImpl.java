/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SchemaException;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class PackageImpl implements Package {
	private QualifiedName qName;
	private Package parentPackage;
	private Schema schema;

	private Map<String, Package> subPackages;
	private Map<String, Domain> domains;
	private Map<String, EdgeClass> edgeClasses;
	private Map<String, VertexClass> vertexClasses;

	public static Package createDefaultPackage(Schema schema) {
		return new PackageImpl("", null, schema);
	}

	private PackageImpl(String simpleName, Package ParentPackage, Schema schema) {
		this.parentPackage = ParentPackage;
		this.schema = schema;
		if (parentPackage == null) {
			if (!simpleName.equals("")) {
				throw new SchemaException(
						"the default package must have empty name");
			}
			qName = new QualifiedName(simpleName);
		} else {
			if (simpleName.equals("")) {
				throw new SchemaException(
						"the simpleName of nested packages must not be empty");
			}
			qName = new QualifiedName(parentPackage.getQualifiedName()
					.toString(), simpleName);
		}
		domains = new TreeMap<String, Domain>();
		edgeClasses = new TreeMap<String, EdgeClass>();
		vertexClasses = new TreeMap<String, VertexClass>();
		subPackages = new TreeMap<String, Package>();
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
	public Package getParentPackage() {
		return parentPackage;
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
		return parentPackage == null;
	}

	@Override
	public String getDirectoryName() {
		if (parentPackage == null) {
			return "";
		}
		return parentPackage.getPathName();
	}

	@Override
	public String getUniqueName() {
		return qName.getUniqueName();
	}

	@Override
	public void setUniqueName(String uniqueName) {
		qName.setUniqueName(this, uniqueName);
	}

	@Override
	public String getPackageName() {
		return qName.getPackageName();
	}

	@Override
	public String getPathName() {
		return qName.getPathName();
	}

	@Override
	public String getQualifiedName() {
		return qName.getQualifiedName();
	}

	@Override
	public String getQualifiedName(Package pkg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSimpleName() {
		return qName.getSimpleName();
	}

	@Override
	public Package createSubPackage(String simpleName) {
		if (isDefaultPackage() && simpleName.equals(SchemaImpl.IMPLPACKAGENAME)) {
			throw new SchemaException("the package name '" + simpleName
					+ "' is forbidden as top level package");
		}
		Package p = getSubPackage(simpleName);
		if (p == null) {
			p = new PackageImpl(simpleName, this, schema);
			subPackages.put(simpleName, p);
			schema.addPackage(p);
		}
		return p;
	}

	@Override
	public boolean containsSubPackage(String simpleName) {
		return subPackages.keySet().contains(simpleName);
	}

	@Override
	public Package getSubPackage(String simpleName) {
		return subPackages.get(simpleName);
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public void addDomain(Domain dom) {
		domains.put(dom.getSimpleName(), dom);

	}

	@Override
	public void addEdgeClass(EdgeClass ec) {
		edgeClasses.put(ec.getSimpleName(), ec);
	}

	@Override
	public void addVertexClass(VertexClass vc) {
		vertexClasses.put(vc.getSimpleName(), vc);
	}

	@Override
	public QualifiedName getQName() {
		return qName;
	}

	@Override
	public String getVariableName() {
		throw new UnsupportedOperationException();
	}
}
