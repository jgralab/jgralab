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
		rootBlock.setVariable("className", schema.getGraphClass().getSimpleName() + "Factory");
		rootBlock.setVariable("simpleClassName", schema.getGraphClass().getSimpleName() + "Factory");
		rootBlock.setVariable("simpleImplClassName", schema.getGraphClass().getSimpleName()+"FactoryImpl");
		rootBlock.setVariable("schemaRootPackage", schemaPackageName);
		rootBlock.setVariable("isClassOnly", "false");
	}

	@Override
	protected CodeBlock createHeader() {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("className", schema.getGraphClass().getSimpleName()+ "Factory");
		code.setVariable("classImplName", schema.getGraphClass().getSimpleName()+"FactoryImpl");
		if(currentCycle.isAbstract()){
			addImports("#jgPackage#.GraphFactory");
			code.add("public interface #className# extends GraphFactory {");
		}else{
			addImports("#schemaRootPackage#.#className#");
			addImports("#jgImplPackage#.GraphFactoryImpl");
			addImports("#jgPackage#.ImplementationType");
			code.add("public class #classImplName# extends GraphFactoryImpl implements #className# {");
		}
		return code;
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.add(createConstructor());
			code.add(createFillTableMethod());
		}
		return code;
	}

	protected CodeBlock createConstructor() {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("classImplName", schema.getGraphClass().getSimpleName()+ "FactoryImpl");
		String implTypeInfix = "";
		if(currentCycle.isStdImpl()) implTypeInfix = "STANDARD";
		if(currentCycle.isTransImpl()) implTypeInfix="TRANSACTION";
		if(currentCycle.isDbImpl()) implTypeInfix = "DATABASE";
		code.add("public #classImplName#() {");
		code.add("\tsuper(ImplementationType."+implTypeInfix+");");
		code.add("\tfillTable();");
		code.add("}");
		return code;
	}

	protected CodeBlock createFillTableMethod() {
		if(currentCycle.isAbstract()){
			return null;
		}
		CodeList code = new CodeList();
		CodeSnippet s = new CodeSnippet(true);
		s.add("protected void fillTable() { ");
		code.addNoIndent(s);

		GraphClass graphClass = schema.getGraphClass();
		code.add(createFillTableForGraph(graphClass));
		for (VertexClass vertexClass : graphClass.getVertexClasses()) {
			code.add(createFillTableForVertex(vertexClass));
		}
		for (EdgeClass edgeClass : graphClass.getEdgeClasses()) {
			code.add(createFillTableForEdge(edgeClass));
		}

		s = new CodeSnippet(true);
		s.add("}");
		code.addNoIndent(s);
		return code;
	}

	protected CodeBlock createFillTableForGraph(GraphClass graphClass) {
		if (graphClass.isAbstract()) {
			return null;
		}

		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("graphName",
				schemaRootPackageName + "." + graphClass.getQualifiedName());
		code.setVariable("graphImplName", schemaRootPackageName + ".impl.std."
				+ graphClass.getQualifiedName());
		code.setVariable("graphTransactionImplName", schemaRootPackageName
				+ ".impl.trans." + graphClass.getQualifiedName());
		code.setVariable("graphDatabaseImplName", schemaRootPackageName
				+ ".impl.db." + graphClass.getQualifiedName());

		if (!graphClass.isAbstract()) {
			code.add("/* code for graph #graphName# */");
			
			if (currentCycle.isStdImpl() && config.hasStandardSupport()) {
				code.add("setGraphImplementationClass(#graphName#.class, #graphImplName#Impl.class);");
			}
			if (currentCycle.isTransImpl()&& config.hasTransactionSupport()) {
				code.add("setGraphImplementationClass(#graphName#.class, #graphTransactionImplName#Impl.class);");
			}
			if (currentCycle.isDbImpl()&&config.hasDatabaseSupport()) {
				code.add("setGraphImplementationClass(#graphName#.class, #graphDatabaseImplName#Impl.class);");
			}
		}
		return code;
	}

	protected CodeBlock createFillTableForVertex(VertexClass vertexClass) {
		if (vertexClass.isAbstract()) {
			return null;
		}

		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("vertexName", schemaRootPackageName + "."
				+ vertexClass.getQualifiedName());
		code.setVariable("vertexImplName", schemaRootPackageName + ".impl.std."
				+ vertexClass.getQualifiedName());
		code.setVariable("vertexTransactionImplName", schemaRootPackageName
				+ ".impl.trans." + vertexClass.getQualifiedName());
		code.setVariable("vertexDatabaseImplName", schemaRootPackageName
				+ ".impl.db." + vertexClass.getQualifiedName());

		if (!vertexClass.isAbstract()) {
			if (currentCycle.isStdImpl() && config.hasStandardSupport()) {
				code.add("setVertexImplementationClass(#vertexName#.class, #vertexImplName#Impl.class);");
			}
			if (currentCycle.isTransImpl() && config.hasTransactionSupport()) {
				code.add("setVertexImplementationClass(#vertexName#.class, #vertexTransactionImplName#Impl.class);");
			}
			if (currentCycle.isDbImpl() && config.hasDatabaseSupport()) {
				code.add("setVertexImplementationClass(#vertexName#.class, #vertexDatabaseImplName#Impl.class);");
			}
		}
		return code;
	}

	protected CodeBlock createFillTableForEdge(EdgeClass edgeClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("edgeName",
				schemaRootPackageName + "." + edgeClass.getQualifiedName());
		code.setVariable("edgeImplName", schemaRootPackageName + ".impl.std."
				+ edgeClass.getQualifiedName());
		code.setVariable("edgeTransactionImplName", schemaRootPackageName
				+ ".impl.trans." + edgeClass.getQualifiedName());
		code.setVariable("edgeDatabaseImplName", schemaRootPackageName
				+ ".impl.db." + edgeClass.getQualifiedName());

		if (!edgeClass.isAbstract()) {
			if (currentCycle.isStdImpl() && config.hasStandardSupport()) {
				code.add("setEdgeImplementationClass(#edgeName#.class, #edgeImplName#Impl.class);");
			}
			if (currentCycle.isTransImpl() && config.hasTransactionSupport()) {
				code.add("setEdgeImplementationClass(#edgeName#.class, #edgeTransactionImplName#Impl.class);");
			}
			if (currentCycle.isDbImpl() && config.hasDatabaseSupport()) {
				code.add("setEdgeImplementationClass(#edgeName#.class, #edgeDatabaseImplName#Impl.class);");
			}
		}
		return code;
	}
}
