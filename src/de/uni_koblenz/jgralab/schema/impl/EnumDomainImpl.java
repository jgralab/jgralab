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

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;
import de.uni_koblenz.jgralab.schema.exception.NoSuchEnumConstantException;

public class EnumDomainImpl extends DomainImpl implements EnumDomain {

	/**
	 * holds a list of the components of the enumeration
	 */
	private List<String> constants;

	/**
	 * @param qn
	 *            the unique name of the enum in the schema
	 * @param constants
	 *            holds a list of the components of the enumeration
	 */
	public EnumDomainImpl(Schema schema, QualifiedName qn,
			List<String> constants) {
		super(schema, qn);
		for (String c : constants) {
			if (!getSchema().isValidEnumConstant(c)) {
				throw new InvalidNameException(c
						+ " is not a valid enumeration constant.");
			}
		}
		this.constants = constants;
	}

	/**
	 * @param qn
	 *            the unique name of the enum in the schema
	 */
	public EnumDomainImpl(Schema schema, QualifiedName qn) {
		super(schema, qn);
		this.constants = new ArrayList<String>();
	}

	@Override
	public String toString() {
		String output = "domain Enum " + getName() + " (";
		String delim = "";
		int count = 0;
		for (String s : constants) {
			output += delim + count++ + ": " + s;
			delim = ", ";
		}
		output += ")";
		return output;
	}

	@Override
	public void addConst(String aConst) {
		if (constants.contains(aConst)) {
			throw new InvalidNameException("Try to add duplicate constant '"
					+ aConst + "' to EnumDomain" + getName());
		}
		if (!getSchema().isValidEnumConstant(aConst)) {
			throw new InvalidNameException(aConst
					+ " is not a valid enumeration constant.");
		}
		constants.add(aConst);
	}

	@Override
	public void deleteConst(String aConst) {
		if (!constants.contains(aConst)) {
			throw new NoSuchEnumConstantException(aConst, getQualifiedName());
		}
		constants.remove(aConst);
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
	public List<String> getConsts() {
		return constants;
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return getQualifiedName(pkg);
	}

	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName) {
		return new CodeSnippet(variableName + " = "
				+ getJavaAttributeImplementationTypeName(schemaPrefix)
				+ ".fromString(" + graphIoVariableName
				+ ".matchEnumConstant());");
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();

		code.add("if (" + variableName + " != null) {");
		code.add("    " + graphIoVariableName + ".writeIdentifier("
				+ variableName + ".toString());");
		code.add("} else {");
		code.add("    " + graphIoVariableName
				+ ".writeIdentifier(\"\\\\null\");");
		code.add("}");

		return code;
	}

	public boolean isComposite() {
		return false;
	}
}
