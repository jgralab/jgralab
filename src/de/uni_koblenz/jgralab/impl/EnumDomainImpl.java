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

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.EnumDomain;
import de.uni_koblenz.jgralab.SchemaException;
import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;

public class EnumDomainImpl extends BasicDomainImpl implements EnumDomain {

	/**
	 * holds a list of the components of the enumeration
	 */
	private List<String> constants;

	/**
	 * @param name
	 *            the unique name of the enum in the schema
	 * @param constants
	 *            holds a list of the components of the enumeration
	 */
	public EnumDomainImpl(String name, List<String> constants) {
		super(name);
		this.constants = constants;
	}

	/**
	 * @param name
	 *            the unique name of the enum in the schema
	 */
	public EnumDomainImpl(String name) {
		super(name);
		this.constants = new ArrayList<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String output = "Enum " + getName();
		String delim = " (";
		int count = 0;
		for (String s : constants) {
			output += delim + count + ": " + s;
			delim = ", ";
		}
		output += ")";
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EnumDomain#addConst(java.lang.String)
	 */
	public void addConst(String aConst)  {
		if (constants.contains(aConst)) {
			throw new SchemaException("Try to add duplicate constant '" + aConst + "' to EnumDomain" + getName());
		}
		constants.add(aConst);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EnumDomain#deleteConst(java.lang.String)
	 */
	public void deleteConst(String aConst) {
		constants.remove(aConst); // nochma testen obs wirklich weg ist!
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
	 * @see jgralab.EnumDomain#getConsts()
	 */
	public List<String> getConsts() {
		return constants;
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
	public CodeBlock getReadMethod(String variableName,
			String graphIoVariableName) {
		return new CodeSnippet(variableName + " = "
				+ getJavaAttributeImplementationTypeName() + ".fromString("
				+ graphIoVariableName + ".matchIdentifier(false));");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#getWriteMethod(java.lang.String, java.lang.String)
	 */
	public CodeBlock getWriteMethod(String variableName,
			String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();
		
		code.add("if (" + variableName + " != null) {");
		code.add("    " + graphIoVariableName + ".writeIdentifier("
				+ variableName + ".toString());");
		code.add("} else {");
		code.add("    " + graphIoVariableName + ".writeIdentifier(\"\\\\null\");");
		code.add("}");
		
		return code;
	}
}
