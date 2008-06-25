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

package de.uni_koblenz.jgralab.schema.impl;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SchemaException;

public class RecordDomainImpl extends CompositeDomainImpl implements
		RecordDomain {

	/**
	 * holds a list of the components of the record
	 */
	private Map<String, Domain> components;

	/**
	 * @param qn
	 *            the unique name of the record in the schema
	 * @param components
	 *            a list of the components of the record
	 */
	public RecordDomainImpl(Schema schema, QualifiedName qn,
			Map<String, Domain> components) {
		super(schema, qn);
		this.components = components;
	}

	/**
	 * @param qn
	 *            the unique name of the record in the schema
	 */
	public RecordDomainImpl(Schema schema, QualifiedName qn) {
		super(schema, qn);
		this.components = new TreeMap<String, Domain>();
	}

	@Override
	public String toString() {
		String output = "Record " + getName();
		String delim = " (";
		for (Entry<String, Domain> c : components.entrySet()) {
			output += delim + c.toString();
			delim = ", ";
		}
		output += ")";
		return output;
	}

	@Override
	public void addComponent(String name, Domain aDomain) {
		if (components.containsKey(name)) {
			throw new SchemaException("duplicate record component '" + name
					+ "' in RecordDomain '" + getName() + "'");
		}
		components.put(name, aDomain);
	}

	@Override
	public void deleteComponent(String name) {
		if (!components.containsKey(name)) {
			throw new SchemaException("RecordDomain '" + getName()
					+ "' does not contain a component '" + name + "'");
		}
		components.remove(name);
	}

	public int getComponentCount() {
		return components.size();
	}

	@Override
	public Domain getDomainOfComponent(String name) {
		return components.get(name);
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return schemaRootPackagePrefix + "." + getQualifiedName();
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public Map<String, Domain> getComponents() {
		return components;
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return getQualifiedName(pkg);
	}

	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();

		code.add("if (!" + graphIoVariableName
				+ ".isNextToken(\"\\\\null\")) {");
		code.add("    " + variableName + " = new "
				+ getJavaAttributeImplementationTypeName(schemaPrefix) + "("
				+ graphIoVariableName + ");");
		code.add("} else {");
		code.add("    io.match(\"\\\\null\");");
		code.add("    " + variableName + " = null;");
		code.add("}");

		return code;
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();

		code.add("if (" + variableName + " != null) {");
		code.add("    " + variableName + ".writeComponentValues("
				+ graphIoVariableName + ");");
		code.add("} else {");
		code.add("    " + graphIoVariableName
				+ ".writeIdentifier(\"\\\\null\");");
		code.add("}");

		return code;
	}

	public Set<Domain> getAllComponentDomains() {
		return new TreeSet<Domain>(components.values());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (!(o instanceof RecordDomain))
			return false;

		RecordDomain other = (RecordDomain) o;
		return components.equals(other.getComponents());
	}
}
