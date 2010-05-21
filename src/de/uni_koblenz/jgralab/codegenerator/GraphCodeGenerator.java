/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

package de.uni_koblenz.jgralab.codegenerator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GraphCodeGenerator extends AttributedElementCodeGenerator {

	public GraphCodeGenerator(GraphClass graphClass, String schemaPackageName,
			String implementationName, String schemaName,
			CodeGeneratorConfiguration config) {
		super(graphClass, schemaPackageName, implementationName, config);
		rootBlock.setVariable("graphElementClass", "Graph");
		rootBlock.setVariable("schemaName", schemaName);
		rootBlock.setVariable("theGraph", "this");
	}

	@Override
	protected CodeBlock createHeader() {
		return super.createHeader();
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = (CodeList) super.createBody();
		if (currentCycle.isStdOrTransImpl()) {
			if (currentCycle.isStdImpl()) {
				addImports("#jgImplStdPackage#.#baseClassName#");
			}
			if (currentCycle.isTransImpl()) {
				addImports("#jgImplTransPackage#.#baseClassName#");
			}
			rootBlock.setVariable("baseClassName", "GraphImpl");
			addImports("de.uni_koblenz.jgralab.Vertex");
			addImports("de.uni_koblenz.jgralab.greql2.jvalue.JValue");
			addImports("de.uni_koblenz.jgralab.greql2.jvalue.JValueSet");
			addImports("de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl");
			addImports("de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator");

			// for Vertex.reachableVertices()
			code
					.add(new CodeSnippet(
							"\nprivate GreqlEvaluator greqlEvaluator = null;\n",
							"@SuppressWarnings(\"unchecked\") ",
							"@Override ",
							"public synchronized <T extends Vertex> Iterable<T> reachableVertices(Vertex startVertex, String pathDescription, Class<T> vertexType) { ",
							"\tif (greqlEvaluator == null) { ",
							"\t\tgreqlEvaluator = new GreqlEvaluator((String) null, this, null); ",
							"\t} ",
							"\tgreqlEvaluator.setVariable(\"v\", new JValueImpl(startVertex)); ",
							"\tgreqlEvaluator.setQuery(\"using v: v \" + pathDescription); ",
							"\tgreqlEvaluator.startEvaluation(); ",
							"\tJValueSet rs = greqlEvaluator.getEvaluationResult().toJValueSet(); ",
							"\tjava.util.List<T> lst = new java.util.LinkedList<T>(); ",
							"\tfor (JValue jv : rs) { ",
							"\t\tlst.add((T) jv.toVertex()); ", "\t\t}",
							"\treturn lst; ", "}"));

		}

		code.add(createGraphElementClassMethods());
		code.add(createEdgeIteratorMethods());
		code.add(createVertexIteratorMethods());
		code.add(createCreateRecordsMethods());
		return code;
	}

	/**
	 * Create "create"-methods for each RecordDomain defined in the schema.
	 * 
	 * @param createClass
	 * @return
	 */
	private CodeBlock createCreateRecordsMethods() {
		CodeList code = new CodeList();

		if (currentCycle.isAbstract()) {
			if (aec.getSchema().getRecordDomains().size() > 0) {
				addImports("java.util.Map");
				addImports("#jgPackage#.GraphIO");
				addImports("#jgPackage#.GraphIOException");
			}
			for (RecordDomain rd : aec.getSchema().getRecordDomains()) {
				CodeSnippet cs = new CodeSnippet(true);
				cs
						.add("public #rcname# create#rname#(GraphIO io) throws GraphIOException;");
				cs.add("");

				cs
						.add("public #rcname# create#rname#(Map<String, Object> fields);");
				cs.add("");

				cs.add("public #rcname# create#rname#(#parawtypes#);");

				cs.setVariable("parawtypes", buildParametersOutput(rd
						.getComponents(), true));
				cs.setVariable("rcname", rd
						.getJavaClassName(schemaRootPackageName));
				cs.setVariable("rname", rd.getUniqueName());
				cs.add("");
				code.addNoIndent(cs);
			}
		}

		if (currentCycle.isStdOrTransImpl()) {
			if (aec.getSchema().getRecordDomains().size() > 0) {
				addImports("java.util.Map");
			}
			for (RecordDomain rd : aec.getSchema().getRecordDomains()) {
				CodeSnippet cs = new CodeSnippet(true);
				cs
						.add("public #rcname# create#rname#(GraphIO io) throws GraphIOException {");
				if (currentCycle.isTransImpl()) {
					cs
							.add("\tif(!isLoading() && getCurrentTransaction().isReadOnly())");
					cs
							.add("\t\tthrow new #jgPackage#.GraphException(\"Read-only transactions are not allowed to create instances of #rtype#.\");");
					cs.add("\treturn createRecord(#rtranstype#.class, io);");
				} else {
					cs.add("\treturn createRecord(#rstdtype#.class, io);");
				}
				cs.add("}");
				cs.add("");

				cs
						.add("public #rcname# create#rname#(Map<String, Object> fields) {");
				if (currentCycle.isTransImpl()) {
					cs
							.add("\tif(!isLoading() && getCurrentTransaction().isReadOnly())");
					cs
							.add("\t\tthrow new #jgPackage#.GraphException(\"Read-only transactions are not allowed to create instances of #rtype#.\");");
					cs
							.add("\treturn createRecord(#rtranstype#.class, fields);");
				} else {
					cs.add("\treturn createRecord(#rstdtype#.class, fields);");
				}
				cs.add("}");
				cs.add("");

				cs.setVariable("parawtypes", buildParametersOutput(rd
						.getComponents(), true));
				cs.setVariable("parawotypes", buildParametersOutput(rd
						.getComponents(), false));

				cs.add("");
				cs.add("public #rcname# create#rname#(#parawtypes#) {");

				if (currentCycle.isTransImpl()) {
					cs
							.add("\tif(!isLoading() && getCurrentTransaction().isReadOnly())");
					cs
							.add("\t\tthrow new #jgPackage#.GraphException(\"Read-only transactions are not allowed to create instances of #rtype#.\");");
					cs
							.add("\treturn createRecord(#rtranstype#.class, #parawotypes#);");
				} else {
					cs
							.add("\treturn createRecord(#rstdtype#.class, #parawotypes#);");
				}
				cs.add("}");
				cs.add("");

				cs.setVariable("rcname", rd
						.getJavaClassName(schemaRootPackageName));
				cs.setVariable("rname", rd.getUniqueName());
				cs
						.setVariable(
								"rtype",
								rd
										.getJavaAttributeImplementationTypeName(schemaRootPackageName));
				cs
						.setVariable(
								"rtranstype",
								rd
										.getTransactionJavaAttributeImplementationTypeName(schemaRootPackageName));
				cs
						.setVariable(
								"rstdtype",
								rd
										.getStandardJavaAttributeImplementationTypeName(schemaRootPackageName));
				code.addNoIndent(cs);
			}
		}
		return code;
	}

	/**
	 * 
	 * @param components
	 * @param withTypes
	 * @return
	 */
	private String buildParametersOutput(
			Collection<RecordComponent> components, boolean withTypes) {
		StringBuilder parameters = new StringBuilder();
		int count = 0;
		int size = components.size();
		for (RecordComponent entry : components) {
			parameters.append(
					(withTypes ? entry.getDomain()
							.getJavaAttributeImplementationTypeName(
									schemaRootPackageName) : "")).append(" _")
					.append(entry.getName());
			count++;
			if (size != count) {
				parameters.append(", ");
			}
		}
		return parameters.toString();
	}

	@Override
	protected CodeBlock createConstructor() {
		addImports("#schemaPackageName#.#schemaName#");
		CodeSnippet code = new CodeSnippet(true);
		if (currentCycle.isTransImpl()) {
			code.setVariable("createSuffix", "WithTransactionSupport");
		}
		if (currentCycle.isStdImpl()) {
			code.setVariable("createSuffix", "");
		}

		code
				.add(
						"/* Constructors and create methods with values for initial vertex and edge count */",
						"public #simpleClassName#Impl(int vMax, int eMax) {",
						"\tthis(null, vMax, eMax);",
						"}",
						"",
						"public #simpleClassName#Impl(java.lang.String id, int vMax, int eMax) {",
						"\tsuper(id, #schemaName#.instance().#schemaVariableName#, vMax, eMax);",
						"\tinitializeAttributesWithDefaultValues();",
						"}",
						"",
						"public static #javaClassName# create(int vMax, int eMax) {",
						"\treturn (#javaClassName#) #schemaName#.instance().create#uniqueClassName##createSuffix#(null, vMax, eMax);",
						"}",
						"",
						"public static #javaClassName# create(String id, int vMax, int eMax) {",
						"\treturn (#javaClassName#) #schemaName#.instance().create#uniqueClassName##createSuffix#(id, vMax, eMax);",
						"}",
						"",
						"/* Constructors and create methods without values for initial vertex and edge count */",
						"public #simpleClassName#Impl() {",
						"\tthis(null);",
						"}",
						"",
						"public #simpleClassName#Impl(java.lang.String id) {",
						"\tsuper(id, #schemaName#.instance().#schemaVariableName#);",
						"\tinitializeAttributesWithDefaultValues();",
						"}",
						"",
						"public static #javaClassName# create() {",
						"\treturn (#javaClassName#) #schemaName#.instance().create#uniqueClassName##createSuffix#(null);",
						"}",
						"",
						"public static #javaClassName# create(String id) {",
						"\treturn (#javaClassName#) #schemaName#.instance().create#uniqueClassName##createSuffix#(id);",
						"}");
		return code;
	}

	private CodeBlock createGraphElementClassMethods() {
		CodeList code = new CodeList();

		GraphClass gc = (GraphClass) aec;
		TreeSet<GraphElementClass> sortedClasses = new TreeSet<GraphElementClass>();
		sortedClasses.addAll(gc.getGraphElementClasses());
		for (GraphElementClass gec : sortedClasses) {
			if (!gec.isInternal()) {
				CodeList gecCode = new CodeList();
				code.addNoIndent(gecCode);

				gecCode
						.addNoIndent(new CodeSnippet(
								true,
								"// ------------------------ Code for #ecQualifiedName# ------------------------"));

				gecCode.setVariable("ecSimpleName", gec.getSimpleName());
				gecCode.setVariable("ecUniqueName", gec.getUniqueName());
				gecCode.setVariable("ecQualifiedName", gec.getQualifiedName());
				gecCode.setVariable("ecSchemaVariableName", gec
						.getVariableName());
				gecCode.setVariable("ecJavaClassName", schemaRootPackageName
						+ "." + gec.getQualifiedName());
				gecCode.setVariable("ecType",
						(gec instanceof VertexClass ? "Vertex" : "Edge"));
				gecCode.setVariable("ecTypeInComment",
						(gec instanceof VertexClass ? "vertex" : "edge"));
				gecCode.setVariable("ecCamelName", camelCase(gec
						.getUniqueName()));
				gecCode.setVariable("ecImplName",
						(gec.isAbstract() ? "**ERROR**" : camelCase(gec
								.getQualifiedName())
								+ "Impl"));

				gecCode.addNoIndent(createGetFirstMethods(gec));
				gecCode.addNoIndent(createFactoryMethods(gec));
			}
		}

		return code;
	}

	private CodeBlock createGetFirstMethods(GraphElementClass gec) {
		CodeList code = new CodeList();
		if (config.hasTypeSpecificMethodsSupport()) {
			code.addNoIndent(createGetFirstMethod(gec, false));
			if (config.hasMethodsForSubclassesSupport()) {
				if (!gec.isAbstract()) {
					code.addNoIndent(createGetFirstMethod(gec, true));
				}
			}
		}
		return code;
	}

	private CodeBlock createGetFirstMethod(GraphElementClass gec,
			boolean withTypeFlag) {
		CodeSnippet code = new CodeSnippet(true);
		if (currentCycle.isAbstract()) {
			code
					.add("/**",
							" * @return the first #ecSimpleName# #ecTypeInComment# in this graph");
			if (withTypeFlag) {
				code
						.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #ecSimpleName# are accepted");
			}
			code
					.add(" */",
							"public #ecJavaClassName# getFirst#ecCamelName##inGraph#(#formalParams#);");
		}
		if (currentCycle.isStdOrTransImpl()) {
			code
					.add(
							"public #ecJavaClassName# getFirst#ecCamelName##inGraph#(#formalParams#) {",
							"\treturn (#ecJavaClassName#)getFirst#ecType#OfClass#inGraph#(#schemaName#.instance().#ecSchemaVariableName##actualParams#);",
							"}");
		}
		code.setVariable("inGraph", (gec instanceof VertexClass ? ""
				: "InGraph"));
		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"
				: ""));
		code
				.setVariable("actualParams", (withTypeFlag ? ", noSubClasses"
						: ""));

		return code;
	}

	private CodeBlock createFactoryMethods(GraphElementClass gec) {
		if (gec.isAbstract()) {
			return null;
		}
		CodeList code = new CodeList();

		code.addNoIndent(createFactoryMethod(gec, false));
		if (currentCycle.isStdOrTransImpl()) {
			code.addNoIndent(createFactoryMethod(gec, true));
		}

		return code;
	}

	private CodeBlock createFactoryMethod(GraphElementClass gec, boolean withId) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("transactionSupport",
				currentCycle.isTransImpl() ? "WithTransactionSupport" : "");
		if (currentCycle.isAbstract()) {
			code
					.add(
							"/**",
							" * Creates a new #ecUniqueName# #ecTypeInComment# in this graph.",
							" *");
			if (withId) {
				code
						.add(" * @param id the <code>id</code> of the #ecTypeInComment#");
			}
			if (gec instanceof EdgeClass) {
				code.add(" * @param alpha the start vertex of the edge",
						" * @param omega the target vertex of the edge");
			}
			code
					.add("*/",
							"public #ecJavaClassName# create#ecCamelName#(#formalParams#);");
		}
		if (currentCycle.isStdOrTransImpl()) {
			code
					.add(
							"public #ecJavaClassName# create#ecCamelName#(#formalParams#) {",
							"\t#ecJavaClassName# new#ecType# = (#ecJavaClassName#) graphFactory.create#ecType##transactionSupport#(#ecJavaClassName#.class, #newActualParams#, this#additionalParams#);",
							"\treturn new#ecType#;", "}");
			code.setVariable("additionalParams", "");
		}

		if (gec instanceof EdgeClass) {
			EdgeClass ec = (EdgeClass) gec;
			String fromClass = ec.getFrom().getVertexClass().getQualifiedName();
			String toClass = ec.getTo().getVertexClass().getQualifiedName();
			if (fromClass.equals("Vertex")) {
				code.setVariable("fromClass", rootBlock
						.getVariable("jgPackage")
						+ "." + "Vertex");
			} else {
				code.setVariable("fromClass", schemaRootPackageName + "."
						+ fromClass);
			}
			if (toClass.equals("Vertex")) {
				code.setVariable("toClass", rootBlock.getVariable("jgPackage")
						+ "." + "Vertex");
			} else {
				code.setVariable("toClass", schemaRootPackageName + "."
						+ toClass);
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
	}

	private CodeBlock createEdgeIteratorMethods() {
		GraphClass gc = (GraphClass) aec;

		CodeList code = new CodeList();
		if (!config.hasTypeSpecificMethodsSupport()) {
			return code;
		}

		Set<EdgeClass> edgeClassSet = new HashSet<EdgeClass>();
		edgeClassSet.addAll(gc.getEdgeClasses());

		for (EdgeClass edge : edgeClassSet) {
			if (edge.isInternal()) {
				continue;
			}

			if (currentCycle.isStdOrTransImpl()) {
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
				s
						.add(" * @return an Iterable for all edges of this graph that are of type #edgeQualifiedName# or subtypes.");
				s.add(" */");
				s
						.add("public Iterable<#edgeJavaClassName#> get#edgeUniqueName#Edges();");
			}
			if (currentCycle.isStdOrTransImpl()) {
				s
						.add("public Iterable<#edgeJavaClassName#> get#edgeUniqueName#Edges() {");
				s
						.add("\treturn new EdgeIterable<#edgeJavaClassName#>(this, #edgeJavaClassName#.class);");
				s.add("}");
			}
			s.add("");
			// getFooIncidences(boolean nosubclasses)
			if (config.hasMethodsForSubclassesSupport()) {
				if (currentCycle.isAbstract()) {
					s.add("/**");
					s
							.add(" * @return an Iterable for all incidence edges of this graph that are of type #edgeQulifiedName#.");
					s.add(" *");
					s
							.add(" * @param noSubClasses toggles wether subclasses of #edgeQualifiedName# should be excluded");
					s.add(" */");
					s
							.add("public Iterable<#edgeJavaClassName#> get#edgeUniqueName#Edges(boolean noSubClasses);");
				}
				if (currentCycle.isStdOrTransImpl()) {
					s
							.add("public Iterable<#edgeJavaClassName#> get#edgeUniqueName#Edges(boolean noSubClasses) {");
					s
							.add("\treturn new EdgeIterable<#edgeJavaClassName#>(this, #edgeJavaClassName#.class, noSubClasses);");
					s.add("}\n");
				}
			}

		}
		return code;
	}

	private CodeBlock createVertexIteratorMethods() {
		GraphClass gc = (GraphClass) aec;

		CodeList code = new CodeList();
		if (!config.hasTypeSpecificMethodsSupport()) {
			return code;
		}

		Set<VertexClass> vertexClassSet = new HashSet<VertexClass>();
		vertexClassSet.addAll(gc.getVertexClasses());

		for (VertexClass vertex : vertexClassSet) {
			if (vertex.isInternal()) {
				continue;
			}

			if (currentCycle.isStdOrTransImpl()) {
				addImports("#jgImplPackage#.VertexIterable");
			}

			CodeSnippet s = new CodeSnippet(true);
			code.addNoIndent(s);

			s.setVariable("vertexQualifiedName", vertex.getQualifiedName());
			s.setVariable("vertexJavaClassName", schemaRootPackageName + "."
					+ vertex.getQualifiedName());
			s.setVariable("vertexCamelName", camelCase(vertex.getUniqueName()));
			// getFooIncidences()
			if (currentCycle.isAbstract()) {
				s.add("/**");
				s
						.add(" * @return an Iterable for all vertices of this graph that are of type #vertexQualifiedName# or subtypes.");
				s.add(" */");
				s
						.add("public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices();");
			}
			if (currentCycle.isStdOrTransImpl()) {
				s
						.add("public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices() {");
				s
						.add("\treturn new VertexIterable<#vertexJavaClassName#>(this, #vertexJavaClassName#.class);");
				s.add("}");
			}
			s.add("");
			// getFooIncidences(boolean nosubclasses)
			if (config.hasMethodsForSubclassesSupport()) {
				if (currentCycle.isAbstract()) {
					s.add("/**");
					s
							.add(" * @return an Iterable for all incidence vertices of this graph that are of type #vertexQualifiedName#.");
					s.add(" *");
					s
							.add(" * @param noSubClasses toggles wether subclasses of #vertexQualifiedName# should be excluded");
					s.add(" */");
					s
							.add("public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices(boolean noSubClasses);");
				}
				if (currentCycle.isStdOrTransImpl()) {
					s
							.add("public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices(boolean noSubClasses) {");
					s
							.add("\treturn new VertexIterable<#vertexJavaClassName#>(this, #vertexJavaClassName#.class, noSubClasses);");
					s.add("}\n");
				}
			}

		}
		return code;
	}

	@Override
	protected void addCheckValidityCode(CodeSnippet code) {
		// just do nothing here
	}
}
