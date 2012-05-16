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

package de.uni_koblenz.jgralab.codegenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GraphCodeGenerator extends
		AttributedElementCodeGenerator<GraphClass, Graph> {

	public GraphCodeGenerator(GraphClass graphClass, String schemaPackageName,
			String schemaName, CodeGeneratorConfiguration config) {
		super(graphClass, schemaPackageName, config);
		rootBlock.setVariable("graphElementClass", "Graph");
		rootBlock.setVariable("schemaElementClass", "GraphClass");
		rootBlock.setVariable("schemaName", schemaName);
		rootBlock.setVariable("theGraph", "this");
		interfaces.add("Graph");
	}

	@Override
	protected String getSchemaTypeName() {
		return "GraphClass";
	}

	@Override
	protected CodeBlock createHeader() {
		return super.createHeader();
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = (CodeList) super.createBody();
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			if (currentCycle.isStdImpl()) {
				addImports("#jgImplStdPackage#.#baseClassName#");
			}
			if (currentCycle.isTransImpl()) {
				addImports("#jgImplTransPackage#.#baseClassName#");
			}
			if (currentCycle.isDbImpl()) {
				addImports("#jgImplDbPackage#.#baseClassName#",
						"#jgImplDbPackage#.GraphDatabase");
			}

			rootBlock.setVariable("baseClassName", "GraphImpl");
		}
		code.add(createGraphElementClassMethods());
		code.add(createEdgeIteratorMethods());
		code.add(createVertexIteratorMethods());
		return code;
	}

	@Override
	protected CodeBlock createConstructor() {
		CodeSnippet code = new CodeSnippet(true);
		if (currentCycle.isTransImpl()) {
			code.setVariable("createSuffix", "TRANSACTION");
		}
		if (currentCycle.isStdImpl()) {
			code.setVariable("createSuffix", "STANDARD");
		}
		if (currentCycle.isDbImpl()) {
			code.setVariable("createSuffix", "DATABASE");
		}
		// TODO if(currentCycle.isDbImpl()) only write ctors and create with
		// GraphDatabase as param.
		if (!currentCycle.isDbImpl()) {
			code.add(
					"/**",
					" * DON'T USE THE CONSTRUCTOR",
					" * For instantiating a Graph, use the Schema and a GraphFactory",
					"**/",
					"public #simpleImplClassName#() {",
					"\tthis(null);",
					"}",
					"",
					"/**",
					" * DON'T USE THE CONSTRUCTOR",
					" * For instantiating a Graph, use the Schema and a GraphFactory",
					"**/",
					"public #simpleImplClassName#(int vMax, int eMax) {",
					"\tthis(null, vMax, eMax);",
					"}",
					"",
					"/**",
					" * DON'T USE THE CONSTRUCTOR",
					" * For instantiating a Graph, use the Schema and a GraphFactory",
					"**/",
					"public #simpleImplClassName#(java.lang.String id, int vMax, int eMax) {",
					"\tsuper(id, #javaClassName#.GC, vMax, eMax);",
					"\tinitializeAttributesWithDefaultValues();",
					"}",
					"",
					"/**",
					" * DON'T USE THE CONSTRUCTOR",
					" * For instantiating a Graph, use the Schema and a GraphFactory",
					"**/",
					"public #simpleImplClassName#(java.lang.String id) {",
					"\tsuper(id, #javaClassName#.GC);",
					"\tinitializeAttributesWithDefaultValues();", "}");
		} else {
			code.add(
					"/**",
					" * DON'T USE THE CONSTRUCTOR",
					" * For instantiating a Graph, use a GraphFactory",
					"**/",
					"public #simpleImplClassName#(java.lang.String id, GraphDatabase graphDatabase) {",
					"\tsuper(id, #javaClassName#.GC, graphDatabase);",
					"\tinitializeAttributesWithDefaultValues();",
					"}",
					"",
					"/**",
					" * DON'T USE THE CONSTRUCTOR",
					" * For instantiating a Graph, use a GraphFactory",
					"**/",
					"public #simpleImplClassName#(java.lang.String id, int vMax, int eMax, GraphDatabase graphDatabase) {",
					"\tsuper(id, vMax, eMax, #javaClassName#.GC, graphDatabase);",
					"\tinitializeAttributesWithDefaultValues();", "}");
		}
		return code;
	}

	private CodeBlock createGraphElementClassMethods() {
		CodeList code = new CodeList();

		GraphClass gc = aec;
		TreeSet<GraphElementClass<?, ?>> sortedClasses = new TreeSet<GraphElementClass<?, ?>>();
		sortedClasses.addAll(gc.getGraphElementClasses());
		for (GraphElementClass<?, ?> gec : sortedClasses) {
			CodeList gecCode = new CodeList();
			code.addNoIndent(gecCode);

			gecCode.addNoIndent(new CodeSnippet(
					true,
					"// ------------------------ Code for #ecQualifiedName# ------------------------"));

			gecCode.setVariable("ecSimpleName", gec.getSimpleName());
			gecCode.setVariable("ecUniqueName", gec.getUniqueName());
			gecCode.setVariable("ecQualifiedName", gec.getQualifiedName());
			gecCode.setVariable("ecSchemaVariableName", gec.getVariableName());
			gecCode.setVariable("ecJavaClassName", schemaRootPackageName + "."
					+ gec.getQualifiedName());
			gecCode.setVariable("ecType",
					(gec instanceof VertexClass ? "Vertex" : "Edge"));
			gecCode.setVariable("ecTypeInComment",
					(gec instanceof VertexClass ? "vertex" : "edge"));
			gecCode.setVariable("ecTypeAecConstant",
					(gec instanceof VertexClass ? "VC" : "EC"));
			gecCode.setVariable("ecCamelName", camelCase(gec.getUniqueName()));
			gecCode.setVariable("ecImplName", (gec.isAbstract() ? "**ERROR**"
					: camelCase(gec.getQualifiedName()) + "Impl"));

			gecCode.addNoIndent(createGetFirstMethods(gec));
			gecCode.addNoIndent(createFactoryMethods(gec));
		}

		return code;
	}

	private CodeBlock createGetFirstMethods(GraphElementClass<?, ?> gec) {
		CodeList code = new CodeList();
		if (config.hasTypeSpecificMethodsSupport()) {
			code.addNoIndent(createGetFirstMethod(gec));
		}
		return code;
	}

	private CodeBlock createGetFirstMethod(GraphElementClass<?, ?> gec) {
		CodeSnippet code = new CodeSnippet(true);
		if (currentCycle.isAbstract()) {
			code.add("/**",
					" * @return the first #ecSimpleName# #ecTypeInComment# in this graph");
			code.add(" */", "public #ecJavaClassName# getFirst#ecCamelName#();");
		}
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.add(
					"public #ecJavaClassName# getFirst#ecCamelName#() {",
					"\treturn (#ecJavaClassName#)getFirst#ecType#(#ecJavaClassName#.#ecTypeAecConstant#);",
					"}");
		}

		return code;
	}

	private CodeBlock createFactoryMethods(GraphElementClass<?, ?> gec) {
		if (gec.isAbstract()) {
			return null;
		}
		CodeList code = new CodeList();
		code.addNoIndent(createFactoryMethod(gec, false));
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.addNoIndent(createFactoryMethod(gec, true));
		}
		return code;
	}

	private CodeBlock createFactoryMethod(GraphElementClass<?, ?> gec,
			boolean withId) {
		CodeSnippet code = new CodeSnippet(true);

		if (currentCycle.isAbstract()) {
			code.add(
					"/**",
					" * Creates a new #ecUniqueName# #ecTypeInComment# in this graph.",
					" *");
			if (withId) {
				code.add(" * @param id the <code>id</code> of the #ecTypeInComment#");
			}
			if (gec instanceof EdgeClass) {
				code.add(" * @param alpha the start vertex of the edge",
						" * @param omega the target vertex of the edge");
			}
			code.add("*/",
					"public #ecJavaClassName# create#ecCamelName#(#formalParams#);");
		}
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.add(
					"public #ecJavaClassName# create#ecCamelName#(#formalParams#) {",
					"\treturn graphFactory.<#ecJavaClassName#> create#ecType#(#ecJavaClassName#.#ecTypeAecConstant#, #newActualParams#, this#additionalParams#);",
					"}");
			code.setVariable("additionalParams", "");
		}

		if (gec instanceof EdgeClass) {
			EdgeClass ec = (EdgeClass) gec;
			String fromClass = ec.getFrom().getVertexClass().getQualifiedName();
			String toClass = ec.getTo().getVertexClass().getQualifiedName();
			if (fromClass.equals("Vertex")) {
				code.setVariable("fromClass", "#jgPackage#.Vertex");
			} else {
				code.setVariable("fromClass", "#schemaPackage#." + fromClass);
			}
			if (toClass.equals("Vertex")) {
				code.setVariable("toClass", "#jgPackage#.Vertex");
			} else {
				code.setVariable("toClass", "#schemaPackage#." + toClass);
			}
			code.setVariable("formalParams", (withId ? "int id, " : "")
					+ "#fromClass# alpha, #toClass# omega");
			code.setVariable("addActualParams", ", alpha, omega");
			code.setVariable("additionalParams", ", alpha, omega");
		} else {
			code.setVariable("formalParams", (withId ? "int id" : ""));
			code.setVariable("addActualParams", "");
		}
		code.setVariable("newActualParams", (withId ? "id" : "0"));
		return code;
		// TODO if isDbImpl() only write two create methods!
	}

	private CodeBlock createEdgeIteratorMethods() {
		GraphClass gc = aec;

		CodeList code = new CodeList();
		if (!config.hasTypeSpecificMethodsSupport()) {
			return code;
		}

		for (EdgeClass edge : gc.getEdgeClasses()) {
			if (currentCycle.isStdOrDbImplOrTransImpl()) {
				addImports("#jgImplPackage#.EdgeIterable");
			}
			CodeSnippet s = new CodeSnippet(true);
			code.addNoIndent(s);

			s.setVariable("edgeUniqueName", camelCase(edge.getUniqueName()));
			s.setVariable("edgeQualifiedName", edge.getQualifiedName());
			s.setVariable("edgeJavaClassName", schemaRootPackageName + "."
					+ edge.getQualifiedName());
			// getFooIncidences()
			if (currentCycle.isAbstract()) {
				s.add("/**");
				s.add(" * @return an Iterable for all edges of this graph that are of type #edgeQualifiedName# or subtypes.");
				s.add(" */");
				s.add("public Iterable<#edgeJavaClassName#> get#edgeUniqueName#Edges();");
			}
			if (currentCycle.isStdOrDbImplOrTransImpl()) {
				s.add("public Iterable<#edgeJavaClassName#> get#edgeUniqueName#Edges() {");
				s.add("\treturn new EdgeIterable<#edgeJavaClassName#>(this, #edgeJavaClassName#.class);");
				s.add("}");
			}
			s.add("");
		}
		return code;
	}

	private CodeBlock createVertexIteratorMethods() {
		GraphClass gc = aec;

		CodeList code = new CodeList();
		if (!config.hasTypeSpecificMethodsSupport()) {
			return code;
		}

		Set<VertexClass> vertexClassSet = new HashSet<VertexClass>();
		vertexClassSet.addAll(gc.getVertexClasses());

		for (VertexClass vertex : vertexClassSet) {
			if (currentCycle.isStdOrDbImplOrTransImpl()) {
				addImports("#jgImplPackage#.VertexIterable");
			}

			CodeSnippet s = new CodeSnippet(true);
			code.addNoIndent(s);
			s.setVariable("vertexQualifiedName", vertex.getQualifiedName());
			s.setVariable("vertexJavaClassName",
					"#schemaPackage#." + vertex.getQualifiedName());
			s.setVariable("vertexCamelName", camelCase(vertex.getUniqueName()));
			if (currentCycle.isAbstract()) {
				s.add("/**");
				s.add(" * @return an Iterable for all vertices of this graph that are of type #vertexQualifiedName# or subtypes.");
				s.add(" */");
				s.add("public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices();");
				s.add("",
						"public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices(#jgPackage#.VertexFilter<#vertexJavaClassName#> filter);");
			}
			if (currentCycle.isStdOrDbImplOrTransImpl()) {
				s.add("public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices() {");
				s.add("\treturn new VertexIterable<#vertexJavaClassName#>(this, #vertexJavaClassName#.VC, null);");
				s.add("}");
				s.add("",
						"public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices(#jgPackage#.VertexFilter<#vertexJavaClassName#> filter) {");
				s.add("\treturn new VertexIterable<#vertexJavaClassName#>(this, #vertexJavaClassName#.VC, filter);");
				s.add("}");
			}
			s.add("");
		}
		return code;
	}

	@Override
	protected void addCheckValidityCode(CodeSnippet code) {
		// just do nothing here
	}

	@Override
	protected CodeBlock createAttributedElementClassConstant() {
		return new CodeSnippet(
				true,
				"public static final #jgSchemaPackage#.#schemaElementClass# GC"
						+ " = #schemaPackageName#.#schemaName#.instance().#schemaVariableName#;");
	}

	@Override
	protected CodeBlock createGetAttributedElementClassMethod() {
		return new CodeSnippet(
				true,
				"@Override",
				"public final #jgSchemaPackage#.#schemaElementClass# getAttributedElementClass() {",
				"\treturn #javaClassName#.GC;", "}");
	}
}
