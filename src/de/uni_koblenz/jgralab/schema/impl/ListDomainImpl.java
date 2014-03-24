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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.impl.TgLexer.Token;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeList;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeSnippet;

public final class ListDomainImpl extends CollectionDomainImpl implements
		ListDomain {

	/**
	 * @param aList
	 *            the list which needs to be converted to a set
	 * @return the list elements in a set (loses order and duplicates)
	 */
	public static Set<Object> toSet(List<Object> aList) {
		return new HashSet<Object>(aList);
	}

	ListDomainImpl(Schema schema, Domain aBaseDomain) {
		super(LISTDOMAIN_NAME + "<"
				+ aBaseDomain.getTGTypeName(schema.getDefaultPackage()) + ">",
				(PackageImpl) schema.getDefaultPackage(), aBaseDomain);
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return LISTDOMAIN_TYPE + "<"
				+ baseDomain.getJavaClassName(schemaRootPackagePrefix) + ">";
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName, boolean withUnsetCheck) {
		CodeList code = new CodeList();
		code.setVariable("init", "");
		internalGetReadMethod(code, schemaPrefix, variableName,
				graphIoVariableName, withUnsetCheck);

		return code;
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return LISTDOMAIN_NAME + "<" + baseDomain.getTGTypeName(pkg) + ">";
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
		return "domain " + LISTDOMAIN_NAME + "<" + baseDomain.toString() + ">";
	}

	private void internalGetReadMethod(CodeList code, String schemaPrefix,
			String variableName, String graphIoVariableName,
			boolean withUnsetCheck) {
		code.setVariable("name", variableName);
		code.setVariable("empty", ListDomain.EMPTY_LIST);
		code.setVariable("basedom",
				getBaseDomain().getJavaClassName(schemaPrefix));
		code.setVariable("basetype", getBaseDomain()
				.getJavaAttributeImplementationTypeName(schemaPrefix));
		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("#init#"));
		if (withUnsetCheck) {
			code.addNoIndent(new CodeSnippet("boolean attrIsSet = true;"));
		}
		code.addNoIndent(new CodeSnippet("if (#io#.isNextToken(#token#.LSQ)) {"));
		code.add(new CodeSnippet(LISTDOMAIN_TYPE
				+ "<#basedom#> $#name# = #empty#;", "#io#.match();",
				"while (!#io#.isNextToken(#token#.RSQ)) {"));
		if (getBaseDomain().isComposite()) {
			code.add(new CodeSnippet("\t#basetype# $#name#Element = null;"));
		} else {
			code.add(new CodeSnippet("\t#basetype# $#name#Element;"));
		}
		code.add(
				getBaseDomain().getReadMethod(schemaPrefix,
						"$" + variableName + "Element", graphIoVariableName,
						false), 1);
		code.add(new CodeSnippet("\t$#name# = $#name#.plus($#name#Element);",
				"}", "#io#.match();", "#name# = $#name#;"));
		code.addNoIndent(new CodeSnippet(
				"} else if (#io#.isNextToken(#token#.NULL_LITERAL)) {"));

		code.add(new CodeSnippet("#io#.match(); ", "#name# = null;"));
		if (withUnsetCheck) {
			code.addNoIndent(new CodeSnippet(
					"} else if (#io#.isNextToken(#token#.UNSET_LITERAL)) {",
					"\t#io#.match();", "\tattrIsSet = false;"));
		}
		code.addNoIndent(new CodeSnippet("} else {",
				"\tthrow new GraphIOException(\"Unknown List value\");", "}"));
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
		code.add(new CodeSnippet("#io#.write(\"[\");",
				"for (#basetype# #element# : #name#) {"));
		code.add(
				getBaseDomain().getWriteMethod(schemaRootPackagePrefix,
						code.getVariable("element"), graphIoVariableName), 1);
		code.add(new CodeSnippet("}", "#io#.write(\"]\");"));
		code.addNoIndent(new CodeSnippet("} else {"));
		code.add(new CodeSnippet(graphIoVariableName
				+ ".writeIdentifier(GraphIO.NULL_LITERAL);"));
		code.addNoIndent(new CodeSnippet("}"));
	}

	@Override
	public String getInitialValue() {
		return "null";
	}

	@Override
	public Object parseGenericAttribute(GraphIO io) throws GraphIOException {
		if (io.isNextToken(Token.UNSET_LITERAL)) {
			io.match();
			return GraphIO.Unset.UNSET;
		} else if (io.isNextToken(Token.LSQ)) {
			PVector<Object> result = JGraLab.vector();
			io.match();
			while (!io.isNextToken(Token.RSQ)) {
				Object listElement = null;
				listElement = getBaseDomain().parseGenericAttribute(io);
				result = result.plus(listElement);
			}
			io.match();
			return result;
		} else if (io.isNextToken(Token.NULL_LITERAL)) {
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
			io.write("[");
			for (Object value : (PVector<Object>) data) {
				getBaseDomain().serializeGenericAttribute(io, value);
			}
			io.write("]");
		} else {
			io.writeIdentifier(GraphIO.NULL_LITERAL);
		}
	}

	@Override
	public boolean isConformValue(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof PVector) {
			for (Object element : (PVector<?>) value) {
				if (!getBaseDomain().isConformValue(element)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
