/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         http://jgralab.uni-koblenz.de
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
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.add(createConstructor());
		}
		return code;
	}

	protected CodeBlock createConstructor() {
		CodeList code = new CodeList();
		if (currentCycle.isStdImpl()) {
			code.setVariable("implTypeInfix", "STANDARD");
		}
		if (currentCycle.isTransImpl()) {
			code.setVariable("implTypeInfix", "TRANSACTION");
		}
		if (currentCycle.isDbImpl()) {
			code.setVariable("implTypeInfix", "DATABASE");
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
		code.setVariable("graphTransactionImplName",
				"#schemaImplTransPackage#." + graphClass.getQualifiedName()
						+ "Impl");
		code.setVariable("graphDatabaseImplName", "#schemaImplDbPackage#."
				+ graphClass.getQualifiedName() + "Impl");

		if (!graphClass.isAbstract()) {
			if (currentCycle.isStdImpl() && config.hasStandardSupport()) {
				code.add("setGraphImplementationClass(#schemaPackage#.#graphName#, #graphImplName#.class);");
			}
			if (currentCycle.isTransImpl() && config.hasTransactionSupport()) {
				code.add("setGraphImplementationClass(#schemaPackage#.#graphName#, #graphTransactionImplName#.class);");
			}
			if (currentCycle.isDbImpl() && config.hasDatabaseSupport()) {
				code.add("setGraphImplementationClass(#schemaPackage#.#graphName#, #graphDatabaseImplName#.class);");
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
		code.setVariable("vertexTransactionImplName",
				"#schemaImplTransPackage#." + vertexClass.getQualifiedName()
						+ "Impl");
		code.setVariable("vertexDatabaseImplName", "#schemaImplDbPackage#."
				+ vertexClass.getQualifiedName() + "Impl");

		if (!vertexClass.isAbstract()) {
			if (currentCycle.isStdImpl() && config.hasStandardSupport()) {
				code.add("setVertexImplementationClass(#schemaPackage#.#vertexName#, #vertexImplName#.class);");
			}
			if (currentCycle.isTransImpl() && config.hasTransactionSupport()) {
				code.add("setVertexImplementationClass(#schemaPackage#.#vertexName#, #vertexTransactionImplName#.class);");
			}
			if (currentCycle.isDbImpl() && config.hasDatabaseSupport()) {
				code.add("setVertexImplementationClass(#schemaPackage#.#vertexName#, #vertexDatabaseImplName#.class);");
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
		code.setVariable("edgeTransactionImplName", "#schemaImplTransPackage#."
				+ edgeClass.getQualifiedName() + "Impl");
		code.setVariable("edgeDatabaseImplName", "#schemaImplDbPackage#."
				+ edgeClass.getQualifiedName() + "Impl");

		if (!edgeClass.isAbstract()) {
			if (currentCycle.isStdImpl() && config.hasStandardSupport()) {
				code.add("setEdgeImplementationClass(#schemaPackage#.#edgeName#, #edgeImplName#.class);");
			}
			if (currentCycle.isTransImpl() && config.hasTransactionSupport()) {
				code.add("setEdgeImplementationClass(#schemaPackage#.#edgeName#, #edgeTransactionImplName#.class);");
			}
			if (currentCycle.isDbImpl() && config.hasDatabaseSupport()) {
				code.add("setEdgeImplementationClass(#schemaPackage#.#edgeName#, #edgeDatabaseImplName#.class);");
			}
		}
		return code;
	}
}
