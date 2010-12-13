/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.schema.impl;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;

public final class EnumDomainImpl extends DomainImpl implements EnumDomain {

	/**
	 * holds a list of the components of the enumeration
	 */
	private final List<String> constants = new ArrayList<String>();

	/**
	 * @param qn
	 *            the unique name of the enum in the schema
	 * @param constants
	 *            holds a list of the components of the enumeration
	 */
	EnumDomainImpl(String sn, Package pkg, List<String> constants) {
		super(sn, pkg);
		for (String c : constants) {
			addConst(c);
		}
	}

	@Override
	public void addConst(String aConst) {
		if (constants.contains(aConst)) {
			throw new InvalidNameException("Try to add duplicate constant '"
					+ aConst + "' to EnumDomain" + getQualifiedName());
		}
		if (!getSchema().isValidEnumConstant(aConst)) {
			throw new InvalidNameException(aConst
					+ " is not a valid enumeration constant.");
		}
		constants.add(aConst);
	}

	@Override
	public List<String> getConsts() {
		return constants;
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
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName) {
		return new CodeSnippet(variableName + " = "
				+ getJavaAttributeImplementationTypeName(schemaPrefix)
				+ ".valueOfPermitNull(" + graphIoVariableName
				+ ".matchEnumConstant());");
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return getQualifiedName(pkg);
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();

		code.add("if (" + variableName + " != null) {");
		code.add("\t" + graphIoVariableName + ".writeIdentifier("
				+ variableName + ".toString());");
		code.add("} else {");
		code.add("\t" + graphIoVariableName
				+ ".writeIdentifier(GraphIO.NULL_LITERAL);");
		code.add("}");

		return code;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("domain Enum "
				+ getQualifiedName() + " (");
		String delim = "";
		int count = 0;
		for (String s : constants) {
			output.append(delim + count++ + ": " + s);
			delim = ", ";
		}
		output.append(")");
		return output.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof EnumDomain) {
			EnumDomain other = (EnumDomain) o;
			if (!getSchema().getQualifiedName().equals(
					other.getSchema().getQualifiedName())) {
				return false;
			}
			return getQualifiedName().equals(other.getQualifiedName())
					&& getConsts().equals(other.getConsts());
		}
		return false;
	}

	@Override
	public CodeBlock getTransactionReadMethod(String schemaPrefix,
			String variableName, String graphIoVariableName) {
		return new CodeSnippet(
				getJavaAttributeImplementationTypeName(schemaPrefix) + " "
						+ variableName + " = "
						+ getJavaAttributeImplementationTypeName(schemaPrefix)
						+ ".valueOfPermitNull(" + graphIoVariableName
						+ ".matchEnumConstant());");
	}

	@Override
	public CodeBlock getTransactionWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		return getWriteMethod(schemaRootPackagePrefix, "get"
				+ CodeGenerator.camelCase(variableName) + "()",
				graphIoVariableName);
	}

	@Override
	public String getTransactionJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public String getTransactionJavaClassName(String schemaRootPackagePrefix) {
		return getJavaClassName(schemaRootPackagePrefix);
	}

	@Override
	public String getVersionedClass(String schemaRootPackagePrefix) {
		return "de.uni_koblenz.jgralab.impl.trans.VersionedReferenceImpl<"
				+ getTransactionJavaAttributeImplementationTypeName(schemaRootPackagePrefix)
				+ ">";
	}

	@Override
	public String getInitialValue() {
		return "null";
	}
}
