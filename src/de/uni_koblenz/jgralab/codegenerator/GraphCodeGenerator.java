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

package de.uni_koblenz.jgralab.codegenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GraphCodeGenerator extends AttributedElementCodeGenerator {

	public GraphCodeGenerator(GraphClass graphClass, String schemaPackageName,
			String implementationName, String schemaName,
			boolean transactionSupport) {
		super(graphClass, schemaPackageName, implementationName,
				transactionSupport);
		rootBlock.setVariable("graphElementClass", "Graph");
		rootBlock.setVariable("schemaName", schemaName);
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		/*
		 * addImports("#schemaPackageName#." +
		 * aec.getSchema().getGraphClass().getSimpleName());
		 */
		return super.createHeader(createClass);
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = (CodeList) super.createBody(createClass);
		if (createClass) {
			if (!transactionSupport)
				addImports("#jgImplStdPackage#.#baseClassName#");
			else
				addImports("#jgImplTransPackage#.#baseClassName#");
			rootBlock.setVariable("baseClassName", "GraphImpl");
		}
		code.add(createGraphElementClassMethods(createClass));
		code.add(createEdgeIteratorMethods(createClass));
		code.add(createVertexIteratorMethods(createClass));
		code.add(createCreateRecordsMethods(createClass));
		return code;
	}

	/**
	 * Create "create"-methods for each RecordDomain defined in the schema.
	 * 
	 * @param createClass
	 * @return
	 */
	private CodeBlock createCreateRecordsMethods(boolean createClass) {
		CodeList code = new CodeList();
		if (!createClass) {
			if (aec.getSchema().getRecordDomains().size() > 0) {
				addImports("java.util.Map");
				addImports("#jgPackage#.GraphIO");
				addImports("#jgPackage#.GraphIOException");
			}
		} else {
			if (aec.getSchema().getRecordDomains().size() > 0)
				addImports("java.util.Map");
		}
		CodeSnippet cs = new CodeSnippet(true);
		if (createClass) {
			// Methods added to interface
			for (RecordDomain rd : aec.getSchema().getRecordDomains()) {
				// create-method for GraphIO
				cs.add("public " + rd.getJavaClassName(schemaRootPackageName)
						+ " create" + rd.getSimpleName()
						+ "(GraphIO io) throws GraphIOException {");
				if (transactionSupport)
					cs
							.add("\treturn new "
									+ rd
											.getTransactionJavaAttributeImplementationTypeName(schemaRootPackageName)
									+ "(io);");
				else
					cs
							.add("\treturn new "
									+ rd
											.getStandardJavaAttributeImplementationTypeName(schemaRootPackageName)
									+ "(io);");
				cs.add("}");
				cs.add("");
				// create-method with Map<String, Object>
				cs.add("public " + rd.getJavaClassName(schemaRootPackageName)
						+ " create" + rd.getSimpleName()
						+ "(Map<String, Object> fields) {");
				if (transactionSupport)
					cs
							.add("\treturn new "
									+ rd
											.getTransactionJavaAttributeImplementationTypeName(schemaRootPackageName)
									+ "(fields);");
				else
					cs
							.add("\treturn new "
									+ rd
											.getStandardJavaAttributeImplementationTypeName(schemaRootPackageName)
									+ "(fields);");

				cs.add("}");
				cs.add("");
				// create-method with record-components as parameters
				String parametersWithTypes = "";
				String parametersWithoutTypes = "";
				int size = rd.getComponents().keySet().size();
				int count = 0;
				for (String component : rd.getComponents().keySet()) {
					parametersWithTypes += rd.getDomainOfComponent(component)
							.getJavaAttributeImplementationTypeName(
									schemaRootPackageName)
							+ " " + component;
					parametersWithoutTypes += component;
					count++;
					if (size != count) {
						parametersWithTypes += ", ";
						parametersWithoutTypes += ", ";
					}
				}
				cs.add("");
				cs.add("public " + rd.getJavaClassName(schemaRootPackageName)
						+ " create" + rd.getSimpleName() + "("
						+ parametersWithTypes + ") {");

				if (transactionSupport)
					cs
							.add("\treturn new "
									+ rd
											.getTransactionJavaAttributeImplementationTypeName(schemaRootPackageName)
									+ "(" + parametersWithoutTypes + ");");
				else
					cs
							.add("\treturn new "
									+ rd
											.getStandardJavaAttributeImplementationTypeName(schemaRootPackageName)
									+ "(" + parametersWithoutTypes + ");");
				cs.add("}");
				cs.add("");
			}
		} else {
			// Methods added to interface
			for (RecordDomain rd : aec.getSchema().getRecordDomains()) {
				cs.add("public " + rd.getJavaClassName(schemaRootPackageName)
						+ " create" + rd.getSimpleName()
						+ "(GraphIO io) throws GraphIOException;");
				cs.add("");
				cs.add("public " + rd.getJavaClassName(schemaRootPackageName)
						+ " create" + rd.getSimpleName()
						+ "(Map<String, Object> fields);");
				String parameters = "";
				int size = rd.getComponents().keySet().size();
				int count = 0;
				for (String component : rd.getComponents().keySet()) {
					parameters += rd.getDomainOfComponent(component)
							.getJavaAttributeImplementationTypeName(
									schemaRootPackageName)
							+ " " + component;
					count++;
					if (size != count)
						parameters += ", ";
				}
				cs.add("");
				cs.add("public " + rd.getJavaClassName(schemaRootPackageName)
						+ " create" + rd.getSimpleName() + "(" + parameters
						+ ");");
				cs.add("");
			}
			cs.add("");
		}
		code.add(cs);
		return code;
	}

	@Override
	protected CodeBlock createConstructor() {
		addImports("#schemaPackageName#.#schemaName#");
		CodeSnippet code = new CodeSnippet(true);
		if (!transactionSupport)
			code.setVariable("createSuffix", "");
		else
			code.setVariable("createSuffix", "WithTransactionSupport");
		code
				.add(
						"/* Constructors and create methods with values for initial vertex and edge count */",
						"public #simpleClassName#Impl(int vMax, int eMax) {",
						"\tthis(null, vMax, eMax);",
						"}",
						"",
						"public #simpleClassName#Impl(java.lang.String id, int vMax, int eMax) {",
						"\tsuper(id, #schemaName#.instance().#schemaVariableName#, vMax, eMax);",
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

	private CodeBlock createGraphElementClassMethods(boolean createClass) {
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

				gecCode.addNoIndent(createGetFirstMethods(gec, createClass));
				gecCode.addNoIndent(createFactoryMethods(gec, createClass));
			}
		}

		return code;
	}

	private CodeBlock createGetFirstMethods(GraphElementClass gec,
			boolean createClass) {
		CodeList code = new CodeList();

		code.addNoIndent(createGetFirstMethod(gec, false, createClass));
		if (CodeGenerator.CREATE_METHODS_WITH_TYPEFLAG) {
			if (!gec.isAbstract()) {
				code.addNoIndent(createGetFirstMethod(gec, true, createClass));
			}
		}
		return code;
	}

	private CodeBlock createGetFirstMethod(GraphElementClass gec,
			boolean withTypeFlag, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		if (!createClass) {
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
		} else {
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

	private CodeBlock createFactoryMethods(GraphElementClass gec,
			boolean createClass) {
		if (gec.isAbstract()) {
			return null;
		}
		CodeList code = new CodeList();

		code.addNoIndent(createFactoryMethod(gec, false, createClass));
		code.addNoIndent(createFactoryMethod(gec, true, createClass));

		return code;
	}

	private CodeBlock createFactoryMethod(GraphElementClass gec,
			boolean withId, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("transactionSupport",
				transactionSupport ? "WithTransactionSupport" : "");
		if (!createClass) {
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
		} else {
			code
					.add(
							"public #ecJavaClassName# create#ecCamelName#(#formalParams#) {",
							"\t#ecJavaClassName# new#ecType# = (#ecJavaClassName#) graphFactory.create#ecType##transactionSupport#(#ecJavaClassName#.class, #newActualParams#, this);",
							"\tadd#ecType#(new#ecType##addActualParams#);",
							"\treturn new#ecType#;", "}");
		}

		if (gec instanceof EdgeClass) {
			EdgeClass ec = (EdgeClass) gec;
			String fromClass = ec.getFrom().getQualifiedName();
			String toClass = ec.getTo().getQualifiedName();
			if (fromClass.equals("Vertex")) {
				code.setVariable("fromClass", rootBlock
						.getVariable("jgPackage")
						+ "." + "Vertex");
			} else {
				code.setVariable("fromClass", schemaRootPackageName + "."
						+ ec.getFrom().getQualifiedName());
			}
			if (toClass.equals("Vertex")) {
				code.setVariable("toClass", rootBlock.getVariable("jgPackage")
						+ "." + "Vertex");
			} else {
				code.setVariable("toClass", schemaRootPackageName + "."
						+ ec.getTo().getQualifiedName());
			}
			code.setVariable("formalParams", (withId ? "int id, " : "")
					+ "#fromClass# alpha, #toClass# omega");
			code.setVariable("addActualParams", ", alpha, omega");
		} else {
			code.setVariable("formalParams", (withId ? "int id" : ""));
			code.setVariable("addActualParams", "");
		}
		code.setVariable("newActualParams", (withId ? "id" : "0"));
		return code;
	}

	private CodeBlock createEdgeIteratorMethods(boolean createClass) {
		GraphClass gc = (GraphClass) aec;

		CodeList code = new CodeList();

		Set<EdgeClass> edgeClassSet = new HashSet<EdgeClass>();
		edgeClassSet.addAll(gc.getEdgeClasses());

		for (EdgeClass edge : edgeClassSet) {
			if (edge.isInternal()) {
				continue;
			}

			if (createClass) {
				addImports("#jgImplPackage#.EdgeIterable");
			}

			CodeSnippet s = new CodeSnippet(true);
			code.addNoIndent(s);

			s.setVariable("edgeUniqueName", camelCase(edge.getUniqueName()));
			s.setVariable("edgeQualifiedName", edge.getQualifiedName());
			s.setVariable("edgeJavaClassName", schemaRootPackageName + "."
					+ edge.getQualifiedName());
			// getFooIncidences()
			if (!createClass) {
				s.add("/**");
				s
						.add(" * @return an Iterable for all edges of this graph that are of type #edgeQualifiedName# or subtypes.");
				s.add(" */");
				s
						.add("public Iterable<#edgeJavaClassName#> get#edgeUniqueName#Edges();");
			} else {
				s
						.add("public Iterable<#edgeJavaClassName#> get#edgeUniqueName#Edges() {");
				s
						.add("\treturn new EdgeIterable<#edgeJavaClassName#>(this, #edgeJavaClassName#.class);");
				s.add("}");
			}
			s.add("");
			// getFooIncidences(boolean nosubclasses)
			if (CodeGenerator.CREATE_METHODS_WITH_TYPEFLAG) {
				if (!createClass) {
					s.add("/**");
					s
							.add(" * @return an Iterable for all incidence edges of this graph that are of type #edgeQulifiedName#.");
					s.add(" *");
					s
							.add(" * @param noSubClasses toggles wether subclasses of #edgeQualifiedName# should be excluded");
					s.add(" */");
					s
							.add("public Iterable<#edgeJavaClassName#> get#edgeUniqueName#Edges(boolean noSubClasses);");
				} else {
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

	private CodeBlock createVertexIteratorMethods(boolean createClass) {
		GraphClass gc = (GraphClass) aec;

		CodeList code = new CodeList();

		Set<VertexClass> vertexClassSet = new HashSet<VertexClass>();
		vertexClassSet.addAll(gc.getVertexClasses());

		for (VertexClass vertex : vertexClassSet) {
			if (vertex.isInternal()) {
				continue;
			}

			if (createClass) {
				addImports("#jgImplPackage#.VertexIterable");
			}

			CodeSnippet s = new CodeSnippet(true);
			code.addNoIndent(s);

			s.setVariable("vertexQualifiedName", vertex.getQualifiedName());
			s.setVariable("vertexJavaClassName", schemaRootPackageName + "."
					+ vertex.getQualifiedName());
			s.setVariable("vertexCamelName", camelCase(vertex.getUniqueName()));
			// getFooIncidences()
			if (!createClass) {
				s.add("/**");
				s
						.add(" * @return an Iterable for all vertices of this graph that are of type #vertexQualifiedName# or subtypes.");
				s.add(" */");
				s
						.add("public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices();");
			} else {
				s
						.add("public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices() {");
				s
						.add("\treturn new VertexIterable<#vertexJavaClassName#>(this, #vertexJavaClassName#.class);");
				s.add("}");
			}
			s.add("");
			// getFooIncidences(boolean nosubclasses)
			if (CodeGenerator.CREATE_METHODS_WITH_TYPEFLAG) {
				if (!createClass) {
					s.add("/**");
					s
							.add(" * @return an Iterable for all incidence vertices of this graph that are of type #vertexQualifiedName#.");
					s.add(" *");
					s
							.add(" * @param noSubClasses toggles wether subclasses of #vertexQualifiedName# should be excluded");
					s.add(" */");
					s
							.add("public Iterable<#vertexJavaClassName#> get#vertexCamelName#Vertices(boolean noSubClasses);");
				} else {
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

	@Override
	protected void setGraphReferenceVariable(CodeSnippet code) {
		code.setVariable("graphreference", "");
	}

}
