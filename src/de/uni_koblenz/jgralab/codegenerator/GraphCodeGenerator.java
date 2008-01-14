/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.GraphElementClass;
import de.uni_koblenz.jgralab.VertexClass;

public class GraphCodeGenerator extends AttributedElementCodeGenerator {

	public GraphCodeGenerator(GraphClass graphClass, String schemaPackageName, String implementationName, String schemaName) {
		super(graphClass, schemaPackageName, implementationName);
		rootBlock.setVariable("graphElementClass", "Graph");
		rootBlock.setVariable("schemaName", schemaName);
		rootBlock.setVariable("schemaPackageName", schemaPackageName);
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		addImports("#jgPackage#.Graph", "#jgPackage#.Vertex",
				"#jgPackage#.Edge", "#jgPackage#.Aggregation",
				"#jgPackage#.Composition");
		return super.createHeader(createClass);
	}
	
	
	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = (CodeList) super.createBody(createClass);
		if (createClass) {
			addImports("#jgImplPackage#.#jgImplementation#.GraphImpl");
			rootBlock.setVariable("baseClassName", "GraphImpl");
		}
		code.add(createGraphElementClassMethods(createClass));
		code.add(createEdgeIteratorMethods(createClass));
		code.add(createVertexIteratorMethods(createClass));
		return code;
	}

	@Override
	protected CodeBlock createConstructor() {
		addImports("#jgPackage#.Schema");
		addImports("#schemaPackageName#.#schemaName#");
		return new CodeSnippet(true,
				"public #className#Impl(Schema schema, int vMax, int eMax) {",
				"\tthis(null, schema, vMax, eMax);",
				"}", 
				"",
				"public #className#Impl(java.lang.String id, Schema schema, int vMax, int eMax) {",
				"\tsuper(id, schema.getGraphClass(\"#className#\"), schema, vMax, eMax);",
				"}",
				"",
			    "public static #className# create(int vMax, int eMax) {",
				"\treturn (#className#) #schemaName#.instance().create#className#(null, vMax, eMax);",
				"}",
				"",
			    "public static #className# create(String id, int vMax, int eMax) {",
//			    "\treturn new #className#Impl(schema, vMax, eMax);",
				"\treturn (#className#) #schemaName#.instance().create#className#(id, vMax, eMax);",
			    "}");
	}
	
	
	private CodeBlock createGraphElementClassMethods(boolean createClass) {
		CodeList code = new CodeList();
	
		GraphClass gc = (GraphClass)aec;
		TreeSet<GraphElementClass> sortedClasses = new TreeSet<GraphElementClass>();
		sortedClasses.addAll(gc.getGraphElementClasses());
		for (GraphElementClass gec: sortedClasses) {
			if (gec.getName() != "Vertex" && gec.getName() != "Edge"
					&& gec.getName() != "Aggregation"
					&& gec.getName() != "Composition") {
				if (createClass) {
					addImports("#schemaPackage#." + gec.getName());
				}
				CodeList gecCode = new CodeList();
				code.addNoIndent(gecCode);
	
				gecCode.addNoIndent(new CodeSnippet(true, "// ------------------------ Code for #ecName# ------------------------"));
				
				gecCode.setVariable("ecName", gec.getName());
				gecCode.setVariable("ecType", (gec instanceof VertexClass ? "Vertex" : "Edge"));
				gecCode.setVariable("ecTypeInComment", (gec instanceof VertexClass ? "vertex" : "edge"));
				gecCode.setVariable("ecCamelName", camelCase(gec.getName()));
				gecCode.setVariable("ecImplName", (gec.isAbstract() ? "**ERROR**" : camelCase(gec.getName()) + "Impl"));
	
				gecCode.addNoIndent(createGetByIdMethod(gec, createClass));
				gecCode.addNoIndent(createGetFirstMethods(gec, createClass));
				gecCode.addNoIndent(createFactoryMethods(gec, createClass));
			}
		}

		return code;
	}

	private CodeBlock createGetByIdMethod(GraphElementClass gec, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		if (!createClass) {
			code.add("/**",
					" * @return the #ecName# #ecTypeInComment# with specified <code>id</code>",
					" */",
					"public #ecName# get#ecCamelName#(int id);");
		} else {
			code.add("public #ecName# get#ecCamelName#(int id) {",
					"\treturn (#ecName#)get#ecType#(id);",
					"}");
		}
		return code;
	}

	private CodeBlock createGetFirstMethods(GraphElementClass gec, boolean createClass) {
		CodeList code = new CodeList();
		
		code.addNoIndent(createGetFirstMethod(gec, false, createClass));
		if (!gec.isAbstract()) {
			code.addNoIndent(createGetFirstMethod(gec, true, createClass));
		}
		return code;
	}

	private CodeBlock createGetFirstMethod(GraphElementClass gec, boolean withTypeFlag, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		if (!createClass) {
			code.add("/**",
					" * @return the first #ecName# #ecTypeInComment# in this graph");
			if (withTypeFlag) {
				code.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #ecName# are accepted");
			}
			code.add(" */",
					"public #ecName# getFirst#ecName##inGraph#(#formalParams#);"
					);
		} else {
			addImports("#jgPackage#." + (gec instanceof VertexClass ? "VertexClass" : "EdgeClass"));
			code.add("public #ecName# getFirst#ecName##inGraph#(#formalParams#) {",
					"\treturn (#ecName#)getFirst#ecType#OfClass#inGraph#((#ecType#Class)getGraphClass().getGraphElementClass(\"#ecName#\")#actualParams#);",
					"}");
		}
		code.setVariable("inGraph", (gec instanceof VertexClass ? "" : "InGraph"));
		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses" : ""));
		code.setVariable("actualParams", (withTypeFlag ? ", noSubClasses" : ""));
		
		return code;
	}


	private CodeBlock createFactoryMethods(GraphElementClass gec, boolean createClass) {
		if (gec.isAbstract()) {
			return null;
		}
		CodeList code = new CodeList();

		code.addNoIndent(createFactoryMethod(gec, false, createClass));
		code.addNoIndent(createFactoryMethod(gec, true, createClass));

		return code;
	}

	private CodeBlock createFactoryMethod(GraphElementClass gec, boolean withId, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		if (!createClass) {
			code.add("/**",
					" * Creates a new #ecName# #ecTypeInComment# in this graph.",
					" *"
					);
			if (withId) {
				code.add(" * @param id the <code>id</code> of the #ecTypeInComment#");
			}
			if (gec instanceof EdgeClass) {
				code.add(" * @param alpha the start vertex of the edge",
						" * @param omega the target vertex of the edge");
			}
			code.add("*/",
					"public #ecName# create#ecCamelName#(#formalParams#);");
		} else {
			code.add("public #ecName# create#ecCamelName#(#formalParams#) {",
				//	"\t#ecName# new#ecType# = new #ecImplName#(#newActualParams#, this);",
					"\t#ecName# new#ecType# = (#ecName#) graphFactory.create#ecType#(#ecName#.class, #newActualParams#, this);",
					"\tadd#ecType#(new#ecType##addActualParams#);",
					"\treturn new#ecType#;",
					"}");
		}

		if (gec instanceof EdgeClass) {
			EdgeClass ec = (EdgeClass) gec;
			String fromClass = ec.getFrom().getName();
			String toClass = ec.getTo().getName();
			addImports((fromClass.equals("Vertex") ? "#jgPackage#." : "#schemaPackage#.") + fromClass,
					(toClass.equals("Vertex") ? "#jgPackage#." : "#schemaPackage#.") + toClass);
			code.setVariable("fromClass", fromClass);
			code.setVariable("toClass", toClass);
			code.setVariable("formalParams", (withId ? "int id, ": "") + "#fromClass# alpha, #toClass# omega");
			code.setVariable("addActualParams", ", alpha, omega");
		} else {
			code.setVariable("formalParams", (withId ? "int id" : "") );
			code.setVariable("addActualParams", "");
		}
		code.setVariable("newActualParams", (withId ? "id": "0"));
		return code;
	}

	private CodeBlock createEdgeIteratorMethods(boolean createClass) {
		GraphClass gc = (GraphClass) aec;

		CodeList code = new CodeList();

		Set<EdgeClass> edgeClassSet = new HashSet<EdgeClass>();
		edgeClassSet.addAll(gc.getEdgeClasses());
		edgeClassSet.addAll(gc.getAggregationClasses());
		edgeClassSet.addAll(gc.getCompositionClasses());
		
		for (EdgeClass edge : edgeClassSet) {
			if (edge.isInternal())
				continue;

			if (createClass)
				addImports("#jgImplPackage#.EdgeIterable");

			CodeSnippet s = new CodeSnippet(true);
			code.addNoIndent(s);

			String targetClassName = edge.getName();
			s.setVariable("edgeClassName", targetClassName);

			/* getFooIncidences() */
			if (!createClass) {
				s.add("/**");
				s.add(" * Returns an iterable for all edges of this vertex that are of type #edgeClassName# or subtypes");
				s.add(" */");
				s.add("public Iterable<? extends #edgeClassName#> get#edgeClassName#Edges();");
			} else {
				s.add("public Iterable<? extends #edgeClassName#> get#edgeClassName#Edges() {");
				s.add("\treturn new EdgeIterable<#edgeClassName#>(this, #edgeClassName#.class);");
				s.add("}");
			}
			s.add("");
			/* getFooIncidences(boolean nosubclasses) */
			if (!createClass) {
				s.add("/**");
				s.add(" * Returns an iterable for all incidence edges of this vertex that are of type #edgeClassName#");
				s.add(" * @param noSubClasses toggles wether subclasses of #edgeClassName# should be excluded");
				s.add(" */");
				s.add("public Iterable<? extends #edgeClassName#> get#edgeClassName#Edges(boolean noSubClasses);");
			} else {
				s.add("public Iterable<? extends #edgeClassName#> get#edgeClassName#Edges(boolean noSubClasses) {");
				s.add("\treturn new EdgeIterable<#edgeClassName#>(this, #edgeClassName#.class, noSubClasses);");
				s.add("}\n");
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
			if (vertex.isInternal())
				continue;

			if (createClass)
				addImports("#jgImplPackage#.VertexIterable");

			CodeSnippet s = new CodeSnippet(true);
			code.addNoIndent(s);

			String targetClassName = vertex.getName();
			s.setVariable("vertexClassName", targetClassName);

			/* getFooIncidences() */
			if (!createClass) {
				s.add("/**");
				s.add(" * Returns an iterable for all vertexs of this vertex that are of type #vertexClassName# or subtypes");
				s.add(" */");
				s.add("public Iterable<? extends #vertexClassName#> get#vertexClassName#Vertices();");
			} else {
				s.add("public Iterable<? extends #vertexClassName#> get#vertexClassName#Vertices() {");
				s.add("\treturn new VertexIterable<#vertexClassName#>(this, #vertexClassName#.class);");
				s.add("}");
			}
			s.add("");
			/* getFooIncidences(boolean nosubclasses) */
			if (!createClass) {
				s.add("/**");
				s.add(" * Returns an iterable for all incidence vertexs of this vertex that are of type #vertexClassName#");
				s.add(" * @param noSubClasses toggles wether subclasses of #vertexClassName# should be excluded");
				s.add(" */");
				s.add("public Iterable<? extends #vertexClassName#> get#vertexClassName#Vertices(boolean noSubClasses);");
			} else {
				s.add("public Iterable<? extends #vertexClassName#> get#vertexClassName#Vertices(boolean noSubClasses) {");
				s.add("\treturn new VertexIterable<#vertexClassName#>(this, #vertexClassName#.class, noSubClasses);");
				s.add("}\n");
			}

		}
		return code;
	}
	
}
