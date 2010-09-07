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

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
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
			String implementationName, CodeGeneratorConfiguration config) {
		super(schemaPackageName, "", config);
		this.schema = schema;
		rootBlock.setVariable("className", schema.getName() + "Factory");
		rootBlock.setVariable("simpleClassName", schema.getName() + "Factory");
		rootBlock.setVariable("isClassOnly", "true");
	}

	@Override
	protected CodeBlock createHeader() {
		addImports("#jgImplPackage#.GraphFactoryImpl");
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("className", schema.getName() + "Factory");
		code.add("public class #className# extends GraphFactoryImpl {");
		return code;
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = new CodeList();
		if (currentCycle.isClassOnly()) {
			code.add(createConstructor());
			code.add(createFillTableMethod());
		}
		return code;
	}

	protected CodeBlock createConstructor() {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("className", schema.getName() + "Factory");
		code.add("public #className#() {");
		code.add("\tsuper();");
		code.add("\tfillTable();");
		code.add("}");
		return code;
	}

	protected CodeBlock createFillTableMethod() {
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
		for (RecordDomain recordDomain : schema.getRecordDomains()) {
			code.add(createFillTableForRecord(recordDomain));
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
		code.setVariable("graphName", schemaRootPackageName + "."
				+ graphClass.getQualifiedName());
		code.setVariable("graphImplName", schemaRootPackageName + ".impl.std."
				+ graphClass.getQualifiedName());
		code.setVariable("graphTransactionImplName", schemaRootPackageName
				+ ".impl.trans." + graphClass.getQualifiedName());
		code.setVariable("graphSaveMemImplName", schemaRootPackageName
				+ ".impl.savemem." + graphClass.getQualifiedName());

		if (!graphClass.isAbstract()) {
			code.add("/* code for graph #graphName# */");
			if (config.hasStandardSupport()) {
				code
						.add("setGraphImplementationClass(#graphName#.class, #graphImplName#Impl.class);");
			}
			if (config.hasTransactionSupport()) {
				code
						.add("setGraphTransactionImplementationClass(#graphName#.class, #graphTransactionImplName#Impl.class);");
			}
			if (config.hasSavememSupport()) {
				code
						.add("setGraphSavememImplementationClass(#graphName#.class, #graphSaveMemImplName#Impl.class);");
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
		code.setVariable("vertexSaveMemImplName", schemaRootPackageName
				+ ".impl.savemem." + vertexClass.getQualifiedName());

		if (!vertexClass.isAbstract()) {
			if (config.hasStandardSupport()) {
				code.add("setVertexImplementationClass(#vertexName#.class, #vertexImplName#Impl.class);");
			}
			if (config.hasTransactionSupport()) {
				code.add("setVertexTransactionImplementationClass(#vertexName#.class, #vertexTransactionImplName#Impl.class);");
			}
			if (config.hasSavememSupport()) {
				code.add("setVertexSavememImplementationClass(#vertexName#.class, #vertexSaveMemImplName#Impl.class);");
			}
		}

		return code;
	}

	
	protected CodeBlock createFillTableForRecord(RecordDomain recordDomain) {

		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("recordName", schemaRootPackageName + "."
				+ recordDomain.getQualifiedName());
		code.setVariable("recordImplName", schemaRootPackageName + ".impl.std."
				+ recordDomain.getQualifiedName());
		code.setVariable("recordTransactionImplName", schemaRootPackageName
				+ ".impl.trans." + recordDomain.getQualifiedName());
		code.setVariable("recordSaveMemImplName", schemaRootPackageName
				+ ".impl.savemem." + recordDomain.getQualifiedName());

		if (config.hasStandardSupport()) {
			code.add("setRecordImplementationClass(#recordName#.class, #recordImplName#Impl.class);");
		}
		if (config.hasTransactionSupport()) {
			code.add("setRecordTransactionImplementationClass(#recordName#.class, #recordTransactionImplName#Impl.class);");
		}
		if (config.hasSavememSupport()) {
			code.add("setRecordSavememImplementationClass(#recordName#.class, #recordSaveMemImplName#Impl.class);");
		}

		return code;
	}

	
	protected CodeBlock createFillTableForEdge(EdgeClass edgeClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("edgeName", schemaRootPackageName + "."
				+ edgeClass.getQualifiedName());
		code.setVariable("edgeImplName", schemaRootPackageName + ".impl.std."
				+ edgeClass.getQualifiedName());
		code.setVariable("edgeTransactionImplName", schemaRootPackageName
				+ ".impl.trans." + edgeClass.getQualifiedName());
		code.setVariable("edgeSaveMemImplName", schemaRootPackageName
				+ ".impl.savemem." + edgeClass.getQualifiedName());

		if (!edgeClass.isAbstract()) {
			if (config.hasStandardSupport()) {
				code
						.add("setEdgeImplementationClass(#edgeName#.class, #edgeImplName#Impl.class);");
			}
			if (config.hasTransactionSupport()) {
				code
						.add("setEdgeTransactionImplementationClass(#edgeName#.class, #edgeTransactionImplName#Impl.class);");
			}
			if (config.hasSavememSupport()) {
				code
						.add("setEdgeSavememImplementationClass(#edgeName#.class, #edgeSaveMemImplName#Impl.class);");
			}
		}

		return code;
	}

}
