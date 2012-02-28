/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
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
import java.util.Iterator;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;

public final class SetDomainImpl extends CollectionDomainImpl implements
		SetDomain {

	SetDomainImpl(Schema schema, Domain aBaseDomain) {
		super(SETDOMAIN_NAME + "<"
				+ aBaseDomain.getTGTypeName(schema.getDefaultPackage()) + ">",
				(PackageImpl) schema.getDefaultPackage(), aBaseDomain);
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return SETDOMAIN_TYPE + "<"
				+ baseDomain.getJavaClassName(schemaRootPackagePrefix) + ">";
		// return "de.uni_koblenz.jgralab.impl.std.JGraLabSetImpl<"
		// + baseDomain.getJavaClassName(schemaRootPackagePrefix) + ">";
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		// return "de.uni_koblenz.jgralab.impl.std.JGraLabSetImpl<"
		// + baseDomain.getJavaClassName(schemaRootPackagePrefix) + ">";
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public CodeBlock getReadMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("init", "");
		internalGetReadMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);

		return code;
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return SETDOMAIN_NAME + "<" + baseDomain.getTGTypeName(pkg) + ">";
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("name", variableName);
		internalGetWriteMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);

		return code;
	}

	@Override
	public String toString() {
		return "domain " + SETDOMAIN_NAME + "<" + baseDomain.toString() + ">";
	}

	private void internalGetReadMethod(CodeList code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName) {
		code.setVariable("name", variableName);
		code.setVariable("empty", SetDomain.EMPTY_SET);
		code.setVariable("basedom",
				getBaseDomain().getJavaClassName(schemaRootPackagePrefix));
		code.setVariable(
				"basetype",
				getBaseDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));
		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("#init#"));
		code.addNoIndent(new CodeSnippet("if (#io#.isNextToken(\"{\")) {"));
		code.add(new CodeSnippet(SETDOMAIN_TYPE
				+ "<#basedom#> $#name# = #empty#;"));
		code.add(new CodeSnippet("#io#.match(\"{\");",
				"while (!#io#.isNextToken(\"}\")) {"));
		if (getBaseDomain().isComposite()) {
			code.add(new CodeSnippet("\t#basetype# $#name#Element = null;"));
		} else {
			code.add(new CodeSnippet("\t#basetype# $#name#Element;"));
		}
		code.add(
				getBaseDomain().getReadMethod(schemaRootPackagePrefix,
						"$" + variableName + "Element", graphIoVariableName), 1);
		code.add(new CodeSnippet("\t$#name# = $#name#.plus($#name#Element);",
				"}", "#io#.match(\"}\");", "#name# = $#name#;"));
		code.addNoIndent(new CodeSnippet(
				"} else if (#io#.isNextToken(GraphIO.NULL_LITERAL)) {"));
		code.add(new CodeSnippet("#io#.match();", "#name# = null;"));
		code.addNoIndent(new CodeSnippet("} else {", "\t#name# = null;", "}"));
	}

	private void internalGetWriteMethod(CodeList code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName) {
		code.setVariable("basedom",
				getBaseDomain().getJavaClassName(schemaRootPackagePrefix));
		code.setVariable(
				"basetype",
				getBaseDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));
		code.setVariable("io", graphIoVariableName);

		String element = variableName + "Element";
		element = element.replace('(', '_');
		element = element.replace(')', '_');
		code.setVariable("element", element);

		code.addNoIndent(new CodeSnippet("if (#name# != null) {"));
		code.add(new CodeSnippet("#io#.writeSpace();", "#io#.write(\"{\");",
				"#io#.noSpace();", "for (#basetype# #element# : #name#) {"));
		code.add(
				getBaseDomain().getWriteMethod(schemaRootPackagePrefix,
						code.getVariable("element"), graphIoVariableName), 1);
		code.add(new CodeSnippet("}", "#io#.write(\"}\");", "#io#.space();"));
		code.addNoIndent(new CodeSnippet("} else {"));
		code.add(new CodeSnippet(graphIoVariableName
				+ ".writeIdentifier(GraphIO.NULL_LITERAL);"));
		code.addNoIndent(new CodeSnippet("}"));
	}

	@Override
	public CodeBlock getTransactionReadMethod(String schemaPrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("init", SETDOMAIN_TYPE + "<#basedom#> #name# = null;");
		internalGetReadMethod(code, schemaPrefix, variableName,
				graphIoVariableName);
		return code;
	}

	@Override
	public CodeBlock getTransactionWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("name", "get" + CodeGenerator.camelCase(variableName)
				+ "()");
		internalGetWriteMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);
		return code;
	}

	@Override
	public String getTransactionJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return SETDOMAIN_TYPE
				+ "<"
				+ baseDomain
						.getTransactionJavaClassName(schemaRootPackagePrefix)
				+ ">";
	}

	@Override
	public String getTransactionJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
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

	@Override
	public Object parseGenericAttribute(GraphIO io) throws GraphIOException {
		if (io.isNextToken("{")) {
			PSet<Object> result = JGraLab.set();
			io.match("{");
			while (!io.isNextToken("}")) {
				Object setElement = null;
				setElement = getBaseDomain().parseGenericAttribute(io);
				result = result.plus(setElement);
			}
			io.match("}");
			return result;
		} else if (io.isNextToken(GraphIO.NULL_LITERAL)) {
			io.match();
			return null;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void serializeGenericAttribute(GraphIO io, Object data)
			throws IOException {
		if (data != null) {
			io.writeSpace();
			io.write("{");
			io.noSpace();
			for (Object value : (PSet<Object>) data) {
				getBaseDomain().serializeGenericAttribute(io, value);
			}
			io.write("}");
			io.space();
		} else {
			io.writeIdentifier(GraphIO.NULL_LITERAL);
		}
	}

	@Override
	public boolean isConformGenericValue(Object value) {
		boolean result = true;
		if (value == null) {
			return result;
		}
		result &= value instanceof PSet;
		if (!result) {
			return false;
		}
		Iterator<?> iterator = ((PSet<?>) value).iterator();
		while (iterator.hasNext() && result) {
			result &= getBaseDomain().isConformGenericValue(iterator.next());
		}
		assert (!iterator.hasNext());
		return result;
	}
}
