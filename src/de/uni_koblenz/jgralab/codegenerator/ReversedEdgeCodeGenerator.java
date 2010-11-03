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

package de.uni_koblenz.jgralab.codegenerator;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ReversedEdgeCodeGenerator extends AttributedElementCodeGenerator {

	public ReversedEdgeCodeGenerator(EdgeClass edgeClass,
			String schemaPackageName, String implementationName,
			CodeGeneratorConfiguration config) {
		super(edgeClass, schemaPackageName, implementationName, config);
		rootBlock.setVariable("graphElementClass", "ReversedEdge");
		rootBlock.setVariable("isImplementationClassOnly", "true");
		rootBlock.setVariable("className", "Reversed"
				+ edgeClass.getSimpleName());
		rootBlock.setVariable("simpleClassName", "Reversed"
				+ edgeClass.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", "Reversed"
				+ edgeClass.getSimpleName() + "Impl");
		rootBlock.setVariable("normalQualifiedClassName", schemaRootPackageName
				+ "." + edgeClass.getQualifiedName());
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = (CodeList) super.createBody();
		if (currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()) {
			rootBlock.setVariable("baseClassName", "ReversedEdgeImpl");

			if (currentCycle.isStdImpl()) {
				addImports("#jgImplStdPackage#.#baseClassName#");
			} else if (currentCycle.isSaveMemImpl()) {
				addImports("#jgImplSaveMemPackage#.#baseClassName#");
			} else if (currentCycle.isTransImpl()) {
				addImports("#jgImplTransPackage#.#baseClassName#");
			} else if (currentCycle.isDbImpl()) {
				addImports("#jgImplDbPackage#.#baseClassName#");
			}

			if (config.hasTypeSpecificMethodsSupport()) {
				code.add(createNextEdgeInGraphMethods());
				code.add(createNextEdgeAtVertexMethods());
			}
			// code.add(createValidRolesMethod());
		}
		return code;
	}

	@Override
	protected CodeBlock createConstructor() {
		// TODO Introduce constants for jgImplStdPackage etc. (refactor)
		if (currentCycle.isStdImpl()) {
			addImports("#jgImplStdPackage#.EdgeImpl", "#jgPackage#.Graph");
		}
		if (currentCycle.isSaveMemImpl()) {
			addImports("#jgImplSaveMemPackage#.EdgeImpl", "#jgPackage#.Graph");
		}
		if (currentCycle.isTransImpl()) {
			addImports("#jgImplTransPackage#.EdgeImpl", "#jgPackage#.Graph");
		}
		if (currentCycle.isDbImpl()) {
			addImports("#jgImplDbPackage#.EdgeImpl", "#jgPackage#.Graph");
		}

		return new CodeSnippet(true, "#className#Impl(EdgeImpl e, Graph g) {",
				"\tsuper(e, g);", "}");
	}

	@Override
	protected CodeBlock createGetter(Attribute a) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", a.getName());
		code.setVariable("type", a.getDomain()
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));
		code.setVariable("isOrGet", a.getDomain().getJavaClassName(
				schemaRootPackageName).equals("Boolean") ? "is" : "get");

		if (currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()) {
			code
					.add(
							"public #type# #isOrGet#_#name#() {",
							"\treturn ((#normalQualifiedClassName#)normalEdge).#isOrGet#_#name#();",
							"}");
		}
		if (currentCycle.isAbstract()) {
			code.add("public #type# #isOrGet#_#name#();");
		}
		return code;
	}

	@Override
	protected CodeBlock createSetter(Attribute a) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", a.getName());
		code.setVariable("type", a.getDomain()
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));

		if (currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()) {
			code
					.add(
							"public void set_#name#(#type# _#name#) {",
							"\t((#normalQualifiedClassName#)normalEdge).set_#name#(_#name#);",
							"}");
		}
		if (currentCycle.isAbstract()) {
			code.add("public void set_#name#(#type# _#name#);");
		}
		return code;
	}

	@Override
	protected CodeBlock createGenericGetter(Set<Attribute> attrSet) {
		return null;
	}

	@Override
	protected CodeBlock createGenericSetter(Set<Attribute> attrSet) {
		return null;
	}

	private CodeBlock createNextEdgeInGraphMethods() {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		for (AttributedElementClass ec : superClasses) {
			if (ec.isInternal()) {
				continue;
			}
			EdgeClass ecl = (EdgeClass) ec;
			code.addNoIndent(createNextEdgeInGraphMethod(ecl, false));
			if (config.hasMethodsForSubclassesSupport()) {
				if (!ecl.isAbstract()) {
					code.addNoIndent(createNextEdgeInGraphMethod(ecl, true));
				}
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeInGraphMethod(EdgeClass ec,
			boolean withTypeFlag) {
		CodeSnippet code = new CodeSnippet(
				true,
				"public #ecName# getNext#ecCamelName#InGraph(#formalParams#) {",
				"\treturn ((#ecName#)normalEdge).getNext#ecCamelName#InGraph(#actualParams#);",
				"}");

		code.setVariable("ecName", schemaRootPackageName + "."
				+ ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"
				: ""));
		code.setVariable("actualParams", (withTypeFlag ? "noSubClasses" : ""));
		return code;
	}

	private CodeBlock createNextEdgeAtVertexMethods() {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		for (AttributedElementClass ec : superClasses) {
			if (ec.isInternal()) {
				continue;
			}
			addImports("#jgPackage#.EdgeDirection");
			EdgeClass ecl = (EdgeClass) ec;
			code.addNoIndent(createNextEdgeAtVertexMethod(ecl, false, false));
			code.addNoIndent(createNextEdgeAtVertexMethod(ecl, true, false));
			if (!ecl.isAbstract()) {
				code
						.addNoIndent(createNextEdgeAtVertexMethod(ecl, false,
								true));
				code.addNoIndent(createNextEdgeAtVertexMethod(ecl, true, true));
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeAtVertexMethod(EdgeClass ec,
			boolean withOrientation, boolean withTypeFlag) {

		CodeSnippet code = new CodeSnippet(
				true,
				"public #ecName# getNext#ecCamelName#(#formalParams#) {",
				"\treturn (#ecName#)getNextEdgeOfClass(#ecName#.class#actualParams#);",
				"}");
		code.setVariable("ecName", schemaRootPackageName + "."
				+ ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
		code.setVariable("formalParams",
				(withOrientation ? "EdgeDirection orientation" : "")
						+ (withOrientation && withTypeFlag ? ", " : "")
						+ (withTypeFlag ? "boolean noSubClasses" : ""));
		code.setVariable("actualParams",
				(withOrientation || withTypeFlag ? ", " : "")
						+ (withOrientation ? "orientation" : "")
						+ (withOrientation && withTypeFlag ? ", " : "")
						+ (withTypeFlag ? "noSubClasses" : ""));
		return code;
	}

	@Override
	protected CodeBlock createStaticImplementationClassField() {
		return null;
	}

	@Override
	protected CodeBlock createFields(Set<Attribute> attrSet) {
		return null;
	}

	@Override
	protected CodeBlock createGetM1ClassMethod() {
		return null;
	}

	@Override
	protected CodeBlock createReadAttributesFromStringMethod(
			Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"public void readAttributeValueFromString(String attributeName, String value) throws GraphIOException {"));
		code
				.add(new CodeSnippet(
						"throw new GraphIOException(\"Can not call readAttributeValuesFromString for reversed Edges.\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createWriteAttributeToStringMethod(
			Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"public String writeAttributeValueToString(String _attributeName) throws IOException, GraphIOException {"));
		code
				.add(new CodeSnippet(
						"throw new GraphIOException(\"Can not call writeAttributeValueToString for reversed Edges.\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createReadAttributesMethod(Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code
				.addNoIndent(new CodeSnippet(true,
						"public void readAttributeValues(GraphIO io) throws GraphIOException {"));
		code
				.add(new CodeSnippet(
						"throw new GraphIOException(\"Can not call readAttributeValues for reversed Edges.\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createWriteAttributesMethod(Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException",
				"java.io.IOException");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"public void writeAttributeValues(GraphIO io) throws GraphIOException, IOException {"));
		code
				.add(new CodeSnippet(
						"throw new GraphIOException(\"Can not call writeAttributeValues for reversed Edges.\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createGetVersionedAttributesMethod(
			SortedSet<Attribute> attributeList) {
		if (currentCycle.isTransImpl()) {
			// delegate to attributes()-method in corresponding normalEdge
			CodeSnippet code = new CodeSnippet();
			code
					.add("protected java.util.Set<de.uni_koblenz.jgralab.trans.VersionedDataObject<?>> attributes() {");
			code.add("\treturn ((EdgeImpl) normalEdge).attributes();");
			code.add("}");
			return code;
		}
		return null;
	}

	// private CodeBlock createValidRolesMethod() {
	// CodeList list = new CodeList();
	// CodeSnippet code = new CodeSnippet(true);
	// code.add("private static Set<String> validRoles;");
	// list.add(code);
	//
	// code = new CodeSnippet(true);
	// code.add("static {");
	// code.add("validRoles = new HashSet<String>();");
	// EdgeClass ec = (EdgeClass) aec;
	// for (String s : ec.getTo().getAllRoles()) {
	// code.add("validRoles.add(\"" + s + "\"");
	// }
	// code.add("}");
	// list.add(code);
	// code = new CodeSnippet(true);
	// code.add("public boolean acceptsRolename(String rolename) {",
	// "\treturn validRoles.contains(rolename);",
	// "}");
	// list.add(code);
	//
	// return list;
	// }

}
