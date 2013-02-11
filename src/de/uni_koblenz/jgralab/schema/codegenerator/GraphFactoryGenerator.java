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

package de.uni_koblenz.jgralab.schema.codegenerator;

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This class generates the code of the GraphElement Factory.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GraphFactoryGenerator extends CodeGenerator {

	private final Schema schema;

	public GraphFactoryGenerator(Schema schema, String schemaPackageName,
			CodeGeneratorConfiguration config) {
		super(schemaPackageName, "", config);
		this.schema = schema;
		rootBlock.setVariable("schemaName", schema.getQualifiedName());
		rootBlock.setVariable("simpleClassName", schema.getGraphClass()
				.getSimpleName() + "Factory");
		rootBlock.setVariable("simpleImplClassName", schema.getGraphClass()
				.getSimpleName() + "FactoryImpl");
		rootBlock.setVariable("isClassOnly", "false");
	}

	@Override
	protected CodeBlock createHeader() {
		CodeSnippet code = new CodeSnippet(true);
		if (currentCycle.isAbstract()) {
			addImports("#jgPackage#.GraphFactory");
			code.add("public interface #simpleClassName# extends GraphFactory {");
		} else {
			addImports("#schemaPackage#.#simpleClassName#");
			addImports("#jgImplPackage#.GraphFactoryImpl");
			addImports("#jgPackage#.ImplementationType");
			code.add("public class #simpleImplClassName# extends GraphFactoryImpl implements #simpleClassName# {");
		}
		return code;
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrDiskv2Impl()){//.isStdImpl()) {
			code.add(createConstructor());
		}
		if(currentCycle.isDiskv2Impl()){
			code.add(createCreateVertexMethod());
			code.add(createCreateEdgeMethod());
			code.add(createRestoreVertexMethod());
			code.add(createRestoreEdgeMethod());
			code.add(createCreateGraphMethod());
		}
		return code;
	}

	protected CodeBlock createConstructor() {
		CodeList code = new CodeList();
		if (currentCycle.isStdImpl()) {
			code.setVariable("implTypeInfix", "STANDARD");
		}else if(currentCycle.isDiskv2Impl()){
			code.setVariable("implTypeInfix", "DISKV2");
		}
		CodeSnippet s = new CodeSnippet(true);
		s.add("public #simpleImplClassName#() {",
				"\tsuper(#schemaName#.instance(), ImplementationType.#implTypeInfix#);",
				"\tcreateMaps();");

		code.addNoIndent(s);
		code.add(createFillTableMethod());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	protected CodeBlock createFillTableMethod() {
		if (currentCycle.isAbstract()) {
			return null;
		}
		CodeList code = new CodeList();
		GraphClass graphClass = schema.getGraphClass();
		code.addNoIndent(createFillTableForGraph(graphClass));
		for (VertexClass vertexClass : graphClass.getVertexClasses()) {
			code.addNoIndent(createFillTableForVertex(vertexClass));
		}
		for (EdgeClass edgeClass : graphClass.getEdgeClasses()) {
			code.addNoIndent(createFillTableForEdge(edgeClass));
		}
		return code;
	}

	protected CodeBlock createFillTableForGraph(GraphClass graphClass) {
		if (graphClass.isAbstract()) {
			return null;
		}

		CodeSnippet code = new CodeSnippet(false);
		code.setVariable("graphName", graphClass.getQualifiedName() + ".GC");
		code.setVariable("graphImplName", "#schemaImplStdPackage#."
				+ graphClass.getQualifiedName() + "Impl");
		code.setVariable("graphDiskv2ImplName", "#schemaImplDiskv2Package#."
				+ graphClass.getQualifiedName() + "Impl");
		//code.setVariable("graphTransactionImplName",
		//		"#schemaImplTransPackage#." + graphClass.getQualifiedName()
		//				+ "Impl");
		//code.setVariable("graphDatabaseImplName", "#schemaImplDbPackage#."
		//		+ graphClass.getQualifiedName() + "Impl");

		if (!graphClass.isAbstract()) {
			if (currentCycle.isStdImpl()) {
				code.add("setGraphImplementationClass(#schemaPackage#.#graphName#, #graphImplName#.class);");
			}else if (currentCycle.isDiskv2Impl()){
				code.add("setGraphImplementationClass(#schemaPackage#.#graphName#, #graphDiskv2ImplName#.class);");				
			}
		}
		return code;
	}

	protected CodeBlock createFillTableForVertex(VertexClass vertexClass) {
		if (vertexClass.isAbstract()) {
			return null;
		}

		CodeSnippet code = new CodeSnippet(false);
		code.setVariable("vertexName", vertexClass.getQualifiedName() + ".VC");

		code.setVariable("vertexImplName", "#schemaImplStdPackage#."
				+ vertexClass.getQualifiedName() + "Impl");
		code.setVariable("vertexDiskv2ImplName", "#schemaImplDiskv2Package#."
				+ vertexClass.getQualifiedName() + "Impl");
		//code.setVariable("vertexTransactionImplName",
		//		"#schemaImplTransPackage#." + vertexClass.getQualifiedName()
		//				+ "Impl");
		//code.setVariable("vertexDatabaseImplName", "#schemaImplDbPackage#."
		//		+ vertexClass.getQualifiedName() + "Impl");

		if (!vertexClass.isAbstract()) {
			if (currentCycle.isStdImpl()) {
				code.add("setVertexImplementationClass(#schemaPackage#.#vertexName#, #vertexImplName#.class);");
			}else if (currentCycle.isDiskv2Impl()){
				code.add("setVertexImplementationClass(#schemaPackage#.#vertexName#, #vertexDiskv2ImplName#.class);");
			}
		}
		return code;
	}

	protected CodeBlock createFillTableForEdge(EdgeClass edgeClass) {
		CodeSnippet code = new CodeSnippet(false);
		code.setVariable("edgeName", edgeClass.getQualifiedName() + ".EC");
		code.setVariable("edgeImplName",
				"#schemaImplStdPackage#." + edgeClass.getQualifiedName()
						+ "Impl");
		code.setVariable("edgeDiskv2ImplName",
				"#schemaImplDiskv2Package#." + edgeClass.getQualifiedName()
						+ "Impl");
		//code.setVariable("edgeTransactionImplName", "#schemaImplTransPackage#."
		//		+ edgeClass.getQualifiedName() + "Impl");
		//code.setVariable("edgeDatabaseImplName", "#schemaImplDbPackage#."
		//		+ edgeClass.getQualifiedName() + "Impl");

		if (!edgeClass.isAbstract()) {
			if (currentCycle.isStdImpl()) {
				code.add("setEdgeImplementationClass(#schemaPackage#.#edgeName#, #edgeImplName#.class);");
			}else if (currentCycle.isDiskv2Impl()){
				code.add("setEdgeImplementationClass(#schemaPackage#.#edgeName#, #edgeDiskv2ImplName#.class);");
			}
		}
		return code;
	}
	
	private CodeBlock createCreateVertexMethod() {
		CodeSnippet s = new CodeSnippet(true);
		s.add("@Override");
		s.add("public <V extends #jgPackage#.Vertex> V createVertex(#jgSchemaPackage#.VertexClass vc, int id, #jgPackage#.Graph g) {");
		s.add("\tV v = super.createVertex(vc, id, g);");
		s.add("\t((#jgImplPackage#.InternalGraph) g).addVertex(v);");
		s.add("\treturn v;");
		s.add("}");
		return s;
	}
	private CodeBlock createCreateEdgeMethod() {
		CodeSnippet s = new CodeSnippet(true);
		s.add("@Override");
		s.add("public <E extends #jgPackage#.Edge> E createEdge(#jgSchemaPackage#.EdgeClass ec, int id, #jgPackage#.Graph g, ");
		s.add("\t\t#jgPackage#.Vertex alpha, #jgPackage#.Vertex omega) {");
		s.add("\tE e = super.createEdge(ec, id, g, alpha, omega);");
		s.add("\t((#jgImplPackage#.InternalGraph) g).addEdge(e, alpha, omega);");
		s.add("\treturn e;");
		s.add("}");
		return s;
	}
	
	private CodeBlock createRestoreVertexMethod() {
		CodeSnippet s = new CodeSnippet(true);
		s.add("@Override");
		s.add("public <V extends #jgPackage#.Vertex> V restoreVertex(#jgSchemaPackage#.VertexClass vc, int id, #jgPackage#.Graph g) {");
		s.add("\tV v = super.createVertex(vc, id, g);");
		s.add("\treturn v;");
		s.add("}");
		return s;
	}
	private CodeBlock createRestoreEdgeMethod() {
		CodeSnippet s = new CodeSnippet(true);
		s.add("@Override");
		s.add("public <E extends #jgPackage#.Edge> E restoreEdge(#jgSchemaPackage#.EdgeClass ec, int id, #jgPackage#.Graph g, ");
		s.add("\t\t#jgPackage#.Vertex alpha, #jgPackage#.Vertex omega) {");
		s.add("\tE e = super.createEdge(ec, id, g, alpha, omega);");
		s.add("\t((#jgImplPackage#.InternalEdge)e.getReversedEdge()).setIncidentVertex(omega);");
		s.add("\treturn e;");
		s.add("}");
		return s;
	}
	
	private CodeBlock createCreateGraphMethod(){
		CodeSnippet s = new CodeSnippet(true);
		s.add("@Override");
		s.add("public <G extends #jgPackage#.Graph> G createGraph(#jgSchemaPackage#.GraphClass gc, String id, ");
		s.add("\t\tint vMax, int eMax) {");
		s.add("\ttry {");
		s.add("\t\t@SuppressWarnings(\"unchecked\")");
		s.add("\t\tG graph = (G) graphConstructor.newInstance(id, vMax, eMax);");
		s.add("\t\tgraph.setGraphFactory(this);");
		s.add("\t\t((#jgImplDiskv2Package#.GraphImpl) graph).initializeStorage();");
		s.add("\t\tgraphCreated = true;");
		s.add("\t\treturn graph;");
		s.add("\t} catch (Exception ex) {");
		s.add("\t\tthrow new #jgSchemaPackage#.exception.SchemaClassAccessException(");
		s.add("\t\t\t\"Cannot create graph of class \"");
		s.add("\t\t\t\t+ graphConstructor.getDeclaringClass()");
		s.add("\t\t\t\t.getCanonicalName(), ex);");
		s.add("\t}");
		s.add("}");
		return s;
	}
}
