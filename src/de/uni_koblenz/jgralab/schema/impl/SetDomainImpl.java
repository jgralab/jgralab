/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

import java.util.HashSet;
import java.util.Set;

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
				schema.getDefaultPackage(), aBaseDomain);
	}

	@Override
	public Set<Domain> getAllComponentDomains() {
		HashSet<Domain> componentDomainSet = new HashSet<Domain>(1);
		componentDomainSet.add(baseDomain);
		return componentDomainSet;
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return "java.util." + SETDOMAIN_NAME + "<"
				+ baseDomain.getJavaClassName(schemaRootPackagePrefix) + ">";
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public CodeBlock getReadMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("init", "");
		internalGetReadMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName, false);

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
			String graphIoVariableName, boolean transactionSupport) {
		code.setVariable("name", variableName);
		code.setVariable("basedom", getBaseDomain().getJavaClassName(
				schemaRootPackagePrefix));
		code.setVariable("basetype",
				getBaseDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));
		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("#init#"));
		code.addNoIndent(new CodeSnippet("if (#io#.isNextToken(\"{\")) {"));
		if (transactionSupport)
			code.add(new CodeSnippet(
					"#name# = graph.createSet(#basedom#.class);"));
		else
			code.add(new CodeSnippet(
					"#name# = new java.util.HashSet<#basedom#>();"));
		code.add(new CodeSnippet("#io#.match(\"{\");",
				"while (!#io#.isNextToken(\"}\")) {",
				"\t#basetype# $#name#Element;"));
		code.add(getBaseDomain().getReadMethod(schemaRootPackagePrefix,
				"$#name#Element", graphIoVariableName), 1);
		code.add(new CodeSnippet("\t#name#.add($#name#Element);", "}",
				"#io#.match(\"}\");", "#io#.space();"));
		code
				.addNoIndent(new CodeSnippet(
						"} else if (#io#.isNextToken(GraphIO.NULL_LITERAL) || #io#.isNextToken(GraphIO.OLD_NULL_LITERAL)) {"));
		code.add(new CodeSnippet("#io#.match();"));
		code.addNoIndent(new CodeSnippet("}"));
	}

	private void internalGetWriteMethod(CodeList code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName) {
		code.setVariable("basedom", getBaseDomain().getJavaClassName(
				schemaRootPackagePrefix));
		code.setVariable("basetype",
				getBaseDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));
		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("if (#name# != null) {"));
		code.add(new CodeSnippet("#io#.writeSpace();", "#io#.write(\"{\");",
				"#io#.noSpace();", "for (#basetype# element: #name#) {"));
		code.add(getBaseDomain().getWriteMethod(schemaRootPackagePrefix,
				"element", graphIoVariableName), 1);
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
		code.setVariable("init", "java.util.Set<#basedom#> #name# = null;");
		internalGetReadMethod(code, schemaPrefix, variableName,
				graphIoVariableName, true);
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
		return "de.uni_koblenz.jgralab.impl.trans.JGraLabSet<"
				+ baseDomain
						.getTransactionJavaClassName(schemaRootPackagePrefix)
				+ ">";
	}

	@Override
	public String getTransactionJavaClassName(String schemaRootPackagePrefix) {
		return "de.uni_koblenz.jgralab.impl.trans.JGraLabSet";
	}

	@Override
	public String getVersionedClass(String schemaRootPackagePrefix) {
		return "de.uni_koblenz.jgralab.impl.trans.VersionedJGraLabCloneableImpl<"
				+ getTransactionJavaAttributeImplementationTypeName(schemaRootPackagePrefix)
				+ ">";
	}

	@Override
	public String getInitialValue() {
		return "null";
	}
}
