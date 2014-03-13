/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

import java.io.IOException;
import java.util.List;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.impl.TgLexer.Token;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.exception.SchemaClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

public final class EnumDomainImpl extends DomainImpl implements EnumDomain {

	/**
	 * holds a list of the components of the enumeration
	 */
	private PVector<String> constants = JGraLab.vector();

	/**
	 * The class object representing the generated interface for this EnumDomain
	 */
	private Class<? extends Object> schemaClass;

	private final ClassLoader schemaClassLoader;

	/**
	 * @param qn
	 *            the unique name of the enum in the schema
	 * @param constants
	 *            holds a list of the components of the enumeration
	 */
	EnumDomainImpl(String sn, PackageImpl pkg, List<String> constants,
			ClassLoader schemaClassLoader) {
		super(sn, pkg);
		this.schemaClassLoader = schemaClassLoader;
		for (String c : constants) {
			addConst(c);
		}
	}

	@Override
	public void addConst(String aConst) {
		SchemaImpl s = (SchemaImpl) getSchema();
		s.assertNotFinished();
		if (constants.contains(aConst)) {
			throw new SchemaException("Try to add duplicate constant '"
					+ aConst + "' to EnumDomain" + getQualifiedName());
		}
		if (!s.isValidEnumConstant(aConst)) {
			throw new SchemaException(aConst
					+ " is not a valid enumeration constant.");
		}
		constants = constants.plus(aConst);
	}

	@Override
	public PVector<String> getConsts() {
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
			String graphIoVariableName, boolean withUnsetCheck) {
		return maybeWrapInUnsetCheck(graphIoVariableName, withUnsetCheck,
				variableName + " = "
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
	public String getInitialValue() {
		return "null";
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public Class<? extends Object> getSchemaClass() {
		if (schemaClass == null) {
			String schemaClassName = getSchema().getPackagePrefix() + "."
					+ getQualifiedName();
			try {
				schemaClass = Class.forName(schemaClassName, true,
						SchemaClassManager.instance(schemaClassLoader,
								getSchema().getQualifiedName()));
			} catch (ClassNotFoundException e) {
				throw new SchemaClassAccessException(
						"Can't load (generated) schema class for EnumDomain '"
								+ getQualifiedName() + "'", e);
			}
		}
		return schemaClass;
	}

	@Override
	public Object parseGenericAttribute(GraphIO io) throws GraphIOException {
		if (io.isNextToken(Token.UNSET)) {
			io.match();
			return GraphIO.Unset.UNSET;
		}
		return io.matchEnumConstant();
	}

	@Override
	public void serializeGenericAttribute(GraphIO io, Object data)
			throws IOException {
		if (data != null) {
			io.writeIdentifier(data.toString());
		} else {
			io.writeIdentifier(GraphIO.NULL_LITERAL);
		}
	}

	@Override
	public void setQualifiedName(String newQName) {
		if (qualifiedName.equals(newQName)) {
			return;
		}
		if (schema.knows(newQName)) {
			throw new SchemaException(newQName
					+ " is already known to the schema.");
		}
		String[] ps = SchemaImpl.splitQualifiedName(newQName);
		String newPackageName = ps[0];
		String newSimpleName = ps[1];
		if (!NamedElementImpl.ATTRELEM_OR_NOCOLLDOMAIN_PATTERN.matcher(
				newSimpleName).matches()) {
			throw new SchemaException("Invalid enum domain name '"
					+ newSimpleName + "'.");
		}

		unregister();

		qualifiedName = newQName;
		simpleName = newSimpleName;
		parentPackage = schema.createPackageWithParents(newPackageName);

		register();
	}

	@Override
	public void delete() {
		schema.assertNotFinished();
		if (!attributes.isEmpty()) {
			throw new SchemaException(
					"Cannot delete enum domain that is still used by attributes: "
							+ attributes);
		}
		parentPackage.domains.remove(simpleName);
		schema.namedElements.remove(qualifiedName);
		schema.domains.remove(qualifiedName);
	}

	@Override
	public boolean isConformValue(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof Enum) {
			if (this.getSchemaClass().isInstance(value)) {
				return true;
			}
			return false;
		}
		return (value instanceof String) && this.getConsts().contains(value);
	}
}
