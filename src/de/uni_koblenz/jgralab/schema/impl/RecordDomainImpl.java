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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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
		for (Domain aDomain : components.values()) {
			if (!isDomainOfSchema(getSchema(), aDomain)) {
				throw new SchemaException(aDomain
						+ " must be a domain of the schema "
						+ getSchema().getQualifiedName());
			}
			if (!isAcyclicAfterCreatingComponent(aDomain, true)) {
				throw new SchemaException(
						"The Creation of a component, which has the type "
								+ aDomain
								+ ", would create a cycle of RecordDomains.");
			}
		}
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
		if (name.equals("")) {
			throw new SchemaException(
					"Cannot create a record component with an empty name.");
		}
		if (components.containsKey(name)) {
			throw new SchemaException("Duplicate record component '" + name
					+ "' in RecordDomain '" + getName() + "'");
		}
		if (!isDomainOfSchema(getSchema(), aDomain)) {
			throw new SchemaException(aDomain
					+ " must be a domain of the schema "
					+ getSchema().getQualifiedName());
		}
		if (!isAcyclicAfterCreatingComponent(aDomain, false)) {
			throw new SchemaException(
					"The Creation of a component, which has the type "
							+ aDomain
							+ ", would create a cycle of RecordDomains.");
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

	@Override
	public Domain getDomainOfComponent(String name) {
		if (!components.containsKey(name)) {
			throw new SchemaException("RecordDomain '" + getName()
					+ "' does not contain a component '" + name + "'");
		}
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
		if (this == o) {
			return true;
		}

		if (!(o instanceof RecordDomain)) {
			return false;
		}

		RecordDomain other = (RecordDomain) o;
		return components.equals(other.getComponents());
	}

	/**
	 * Tests if there wouln't be a cyclic inclusion of RecordDomains, if a
	 * component of the Domain <code>domain</code> would be created.<br>
	 * <br>
	 * <b>pattern:</b> b=record.isAcyclicAfterCreatingComponent(domain)<br>
	 * <b>pre:</b> domain.getSchema()=this.getSchema();<br>
	 * <b>post:</b> true, iff all RecordDomains of this schema doesn't contain
	 * themselves in a cyclic way after creating a a component of type
	 * <code>domain</code><br>
	 * false, otherwise<br>
	 * <b>post:</b> record'.equals(record) && domain'.equals(domain)
	 *
	 * @param domain
	 *            the domain of the component
	 * @param fromConstructor
	 *            true, if this method is called from the constructor. false,
	 *            otherwise
	 * @return true, iff all RecordDomains of this schema doesn't contain
	 *         themselves in a cyclic way after creating a a component of type
	 *         <code>domain</code><br>
	 *         false, otherwise<br>
	 */
	protected boolean isAcyclicAfterCreatingComponent(Domain domain,
			boolean fromConstructor) {
		if (!(domain instanceof RecordDomain)) {
			return true;
		} else {
			RecordDomain recDom = (RecordDomain) domain;
			// Creates a Map of all RecordDomains and the number of references
			// from other RecordDomains on them
			HashMap<RecordDomain, Integer> in = new HashMap<RecordDomain, Integer>();
			// Simulation of the situation, that this RecordDomain would have a
			// component of type domain
			if (fromConstructor) {
				in.put(recDom, 0);
			} else {
				in.put(recDom, 1);
			}
			for (Domain d : getSchema().getCompositeDomainsInTopologicalOrder()) {
				if (d instanceof RecordDomain) {
					if (!in.containsKey(d)) {
						in.put((RecordDomain) d, 0);
					}
					for (Domain e : ((RecordDomain) d)
							.getAllComponentCompositeDomains()) {
						if (e instanceof RecordDomain) {
							if (!in.containsKey(e)) {
								in.put((RecordDomain) e, 1);
							} else {
								in.put((RecordDomain) e, in.get(e) + 1);
							}
						}
					}
				}
			}
			// Creates a queue of all RecordDomains, which aren't Domains in
			// other RecordDomains
			Queue<RecordDomain> q = new LinkedList<RecordDomain>();
			for (RecordDomain rec : in.keySet()) {
				if (in.get(rec) == 0) {
					q.add(rec);
				}
			}
			// Creates a topological list of all RecordDomains
			LinkedList<RecordDomain> topologicalList = new LinkedList<RecordDomain>();
			while (!q.isEmpty()) {
				RecordDomain rec = q.poll();
				topologicalList.add(rec);
				for (Domain d : rec.getAllComponentCompositeDomains()) {
					if (d instanceof RecordDomain) {
						RecordDomain dom = (RecordDomain) d;
						int i = in.get(dom) - 1;
						in.put(dom, i);
						if (i == 0) {
							q.add(dom);
						}
					}
				}
			}
			// If topologicalList contains all RecordDomains, then it is
			// acyclic.
			if (topologicalList.size() == in.size()) {
				return true;
			} else {
				return false;
			}
		}
	}
}
