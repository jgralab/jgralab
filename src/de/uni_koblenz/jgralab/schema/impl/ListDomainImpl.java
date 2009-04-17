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
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;

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
		return "java.util." + LISTDOMAIN_NAME + "<"
				+ baseDomain.getJavaClassName(schemaRootPackagePrefix) + ">";
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("name", variableName);
		code.setVariable("tmpname", variableName + "Tmp");
		code.setVariable("basedom", getBaseDomain().getJavaClassName(
				schemaPrefix));
		code.setVariable("basetype", getBaseDomain()
				.getJavaAttributeImplementationTypeName(schemaPrefix));
		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet(
				"if (!#io#.isNextToken(\"\\\\null\")) {"));
		code
				.add(new CodeSnippet(
						"java.util.LinkedList<#basedom#> #tmpname# = new java.util.LinkedList<#basedom#>();",
						"#io#.match(\"[\");",
						"while (!#io#.isNextToken(\"]\")) {",
						"\t#basetype# #name#Element;"));
		code.add(getBaseDomain().getReadMethod(schemaPrefix,
				variableName + "Element", graphIoVariableName), 1);
		code
				.add(new CodeSnippet(
						"\t#tmpname#.add(#name#Element);",
						"}",
						"#io#.match(\"]\");",
						"#name# = new java.util.ArrayList<#basedom#>(#tmpname#.size());",
						"#name#.addAll(#tmpname#);"));
		code.addNoIndent(new CodeSnippet("} else {"));
		code.add(new CodeSnippet("io.match(\"\\\\null\");", "#name# = null;"));
		code.addNoIndent(new CodeSnippet("}"));
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
		code.setVariable("basedom", getBaseDomain().getJavaClassName(
				schemaRootPackagePrefix));
		code.setVariable("basetype",
				getBaseDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));
		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("if (#name# != null) {"));
		code.add(new CodeSnippet("#io#.writeSpace();", "#io#.write(\"[\");",
				"#io#.noSpace();", "for (#basetype# #name#Element: #name#) {"));
		code.add(getBaseDomain().getWriteMethod(schemaRootPackagePrefix,
				variableName + "Element", graphIoVariableName), 1);
		code.add(new CodeSnippet("}", "#io#.write(\"]\");", "#io#.space();"));
		code.addNoIndent(new CodeSnippet("} else {"));
		code.add(new CodeSnippet(graphIoVariableName
				+ ".writeIdentifier(\"\\\\null\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	public String toString() {
		return "domain " + LISTDOMAIN_NAME + "<" + baseDomain.toString() + ">";
	}

}
