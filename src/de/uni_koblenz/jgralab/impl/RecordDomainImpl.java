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
 
package de.uni_koblenz.jgralab.impl;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Domain;
import de.uni_koblenz.jgralab.RecordDomain;
import de.uni_koblenz.jgralab.SchemaException;
import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;

public class RecordDomainImpl extends CompositeDomainImpl implements RecordDomain {

	/**
	 * holds a list of the components of the record
	 */
	private Map<String, Domain> components;

	/**
	 * @param name
	 *            the unique name of the record in the schema
	 * @param components
	 *            a list of the components of the record
	 */
	public RecordDomainImpl(String name, Map<String, Domain> components) {
		super(name);
		this.components = components;
	}

	/**
	 * @param name
	 *            the unique name of the record in the schema
	 */
	public RecordDomainImpl(String name) {
		super(name);
		this.components = new TreeMap<String, Domain>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.RecordDomain#addComponent(java.lang.String, jgralab.Domain)
	 */
	public void addComponent(String name, Domain aDomain)  {
		if (components.containsKey(name)) {
			throw new SchemaException("duplicate record component '" + name
					+ "' in RecordDomain '" + getName() + "'");
		}
		components.put(name, aDomain);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.RecordDomain#deleteComponent(java.lang.String)
	 */
	public void deleteComponent(String name)  {
		if (!components.containsKey(name)) {
			throw new SchemaException("RecordDomain '" + getName() + "' does not contain a component '" + name + "'");
		}
		components.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.RecordDomain#getComponentCount()
	 */
	public int getComponentCount() {
		return components.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.RecordDomain#getDomain(java.lang.String)
	 */
	public Domain getDomainOfComponent(String name) {
		return components.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#toJavaString()
	 */
	public String getJavaAttributeImplementationTypeName() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#toJavaStringNonPrimitive()
	 */
	public String getJavaClassName() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.RecordDomain#getRecordDomainComponents()
	 */
	public Map<String, Domain> getComponents() {
		return components;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#toTGString()
	 */
	public String getTGTypeName() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#getReadMethod(java.lang.String, java.lang.String)
	 */
	public CodeBlock getReadMethod(String variableName, String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();
		
		code.add("if (!" + graphIoVariableName + ".isNextToken(\"\\\\null\")) {");
		code.add("    " + variableName + " = new " 
				+ getJavaAttributeImplementationTypeName() + "("
				+ graphIoVariableName + ");");
		code.add("} else {");
		code.add("    io.match(\"\\\\null\");");
		code.add("    " + variableName + " = null;");
		code.add("}");
		
		return code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#getReadMethod(java.lang.String, java.lang.String)
	 */
	public CodeBlock getWriteMethod(String variableName, String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();
		
		code.add("if (" + variableName + " != null) {");
		code.add("    " + variableName + ".writeComponentValues("
				+ graphIoVariableName + ");");
		code.add("} else {");
		code.add("    " + graphIoVariableName + ".writeIdentifier(\"\\\\null\");");
		code.add("}");
		
		return code;
	}
	
	public Set<Domain> getAllComponentDomains() {
		return new TreeSet<Domain>(components.values());
	}
}
