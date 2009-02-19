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

import java.util.List;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * TODO add comment
 *
 * @author ist@uni-koblenz.de
 *
 */
public class SchemaCodeGenerator extends CodeGenerator {

	private Schema schema;

	/**
	 * Creates a new SchemaCodeGenerator which creates code for the given schema
	 *
	 * @param schema
	 *            the schema to create the code for
	 * @param schemaPackageName
	 *            the package the schema is located in
	 * @param implementationName
	 *            the special jgralab package name to use
	 */
	public SchemaCodeGenerator(Schema schema, String schemaPackageName,
			String implementationName) {
		super(schemaPackageName, "");
		this.schema = schema;

		rootBlock.setVariable("simpleClassName", schema.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", schema.getSimpleName());
		rootBlock.setVariable("baseClassName", "SchemaImpl");
		rootBlock.setVariable("isAbstractClass", "false");
		rootBlock.setVariable("isClassOnly", "true");
		rootBlock.setVariable("isImplementationClassOnly", "false");
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		addImports("#jgSchemaImplPackage#.#baseClassName#");
		addImports("#jgSchemaImplPackage#.ConstraintImpl");
		CodeSnippet code = new CodeSnippet(
				true,
				"/**",
				" * The schema #simpleClassName# is implemented following the singleton pattern.",
				" * To get the instance, use the static method <code>instance()</code>.",
				" */",
				"public class #simpleClassName# extends #baseClassName# {");
		return code;
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = new CodeList();
		code.add(createVariables());
		code.add(createConstructor());
		code.add(createGraphFactoryMethods());
		return code;
	}

	private CodeBlock createGraphFactoryMethods() {
		List<GraphClass> graphClasses = schema
				.getGraphClassesInTopologicalOrder();
		if (graphClasses.size() == 0) {
			return null;
		}
		CodeList code = new CodeList();
		for (GraphClass gc : graphClasses) {
			if (!gc.isInternal()) {
				code.addNoIndent(createGraphFactoryMethods(gc));
			}
		}
		return code;
	}

	private CodeBlock createGraphFactoryMethods(GraphClass gc) {
		addImports("#jgPackage#.Graph", "#jgPackage#.ProgressFunction",
				"#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		CodeSnippet code = new CodeSnippet(
				true,
				"/**",
				" * Creates a new #gcName# graph with initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
				" *",
				" * @param vMax initial vertex count",
				" * @param eMax initial edge count",
				"*/",
				"public #gcName# create#gcCamelName#(int vMax, int eMax) {",
				"\treturn (#gcCamelName#) graphFactory.createGraph(#gcCamelName#.class, null, vMax, eMax);",
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph with the ID <code>id</code> initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
				" *",
				" * @param id the id name of the new graph",
				" * @param vMax initial vertex count",
				" * @param eMax initial edge count",
				" */",
				"public #gcName# create#gcCamelName#(String id, int vMax, int eMax) {",
				"\treturn (#gcCamelName#) graphFactory.createGraph(#gcCamelName#.class, id, vMax, eMax);",
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph.",
				"*/",
				"public #gcName# create#gcCamelName#() {",
				"\treturn (#gcCamelName#) graphFactory.createGraph(#gcCamelName#.class, null);",
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph with the ID <code>id</code>.",
				" *",
				" * @param id the id name of the new graph",
				" */",
				"public #gcName# create#gcCamelName#(String id) {",
				"\treturn (#gcCamelName#) graphFactory.createGraph(#gcCamelName#.class, id);",
				"}",
				"",
				"/**",
				" * Loads a #gcName# graph from the file <code>filename</code>.",
				" *",
				" * @param filename the name of the file",
				" * @return the loaded #gcName#",
				" * @throws GraphIOException if the graph cannot be loaded",
				" */",
				"public #gcName# load#gcCamelName#(String filename) throws GraphIOException {",
				"\treturn load#gcCamelName#(filename, null);",
				"}",
				"",
				"/**",
				" * Loads a #gcName# graph from the file <code>filename</code>.",
				" *",
				" * @param filename the name of the file",
				" * @param pf a progress function to monitor graph loading",
				" * @return the loaded #gcName#",
				" * @throws GraphIOException if the graph cannot be loaded",
				" */",
				"public #gcName# load#gcCamelName#(String filename, ProgressFunction pf) throws GraphIOException {",
				"\tGraph graph = GraphIO.loadGraphFromFile(filename, this, pf);",
				"\tif (!(graph instanceof #gcName#)) {",
				"\t\tthrow new GraphIOException(\"Graph in file '\" + filename + \"' is not an instance of GraphClass #gcName#\");",
				"\t}", "\treturn (#gcName#) graph;", "}");

		code.setVariable("gcName", gc.getQualifiedName());
		code.setVariable("gcCamelName", camelCase(gc.getQualifiedName()));
		code.setVariable("gcImplName", gc.getQualifiedName() + "Impl");
		return code;
	}

	private CodeBlock createConstructor() {
		CodeList code = new CodeList();
		addImports("#jgSchemaPackage#.QualifiedName");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"/**",
						" * the singleton instance",
						" */",
						"static #simpleClassName# theInstance = null;",
						"",
						"/**",
						" * @return the singleton instance of #simpleClassName#",
						" */",
						"public static #simpleClassName# instance() {",
						"\tif (theInstance == null) {",
						"\t\ttheInstance = new #simpleClassName#();",
						"\t}",
						"\treturn theInstance;",
						"}",
						"",
						"/**",
						" * Creates a #simpleClassName# and builds its schema classes.",
						" * This constructor is private. Use the <code>instance()</code> method",
						" * to acess the schema.", " */",
						"private #simpleClassName#() {",
						"\tsuper(new QualifiedName(\"#schemaPackage#\", \"#simpleClassName#\"));"));

		code.add(new CodeSnippet("vc_Vertex = getDefaultVertexClass();"));
		code.add(createEnumDomains());
		code.add(createCompositeDomains());
		code.add(createGraphClasses());
		addImports("#schemaPackage#.impl.#simpleClassName#Factory");
		code.add(new CodeSnippet(true,
				"graphFactory = new #simpleClassName#Factory();"));
		code.addNoIndent(new CodeSnippet(true, "}"));

		return code;
	}

	private CodeBlock createGraphClasses() {
		CodeList code = new CodeList();
		for (GraphClass gc : schema.getGraphClassesInTopologicalOrder()) {
			if (!gc.isInternal()) {
				code.addNoIndent(createGraphClass(gc));
			}
		}
		return code;
	}

	private CodeBlock createGraphClass(GraphClass gc) {
		CodeList code = new CodeList();
		addImports("#jgSchemaPackage#.GraphClass");
		code.setVariable("gcName", gc.getQualifiedName());
		code.setVariable("gcVariable", "gc");
		code.setVariable("aecVariable", "gc");
		code.setVariable("schemaVariable", gc.getVariableName());
		code.setVariable("gcAbstract", gc.isAbstract() ? "true" : "false");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"{",
						"\tGraphClass #gcVariable# = #schemaVariable# = createGraphClass(new QualifiedName(\"#gcName#\"));",
						"\t#gcVariable#.setAbstract(#gcAbstract#);"));
		for (AttributedElementClass superClass : gc.getDirectSuperClasses()) {
			if (superClass.isInternal()) {
				continue;
			}
			CodeSnippet s = new CodeSnippet(
					"#gcVariable#.addSuperClass(getGraphClass(new QualifiedName(\"#superClassName#\")));");
			s.setVariable("superClassName", superClass.getQualifiedName());
			code.add(s);
		}
		code.add(createAttributes(gc));
		code.add(createConstraints(gc));
		code.add(createVertexClasses(gc));
		code.add(createEdgeClasses(gc));
		code.addNoIndent(new CodeSnippet(true, "}"));
		return code;
	}

	private CodeBlock createVariables() {
		CodeList code = new CodeList();
		for (GraphClass gc : schema.getGraphClassesInTopologicalOrder()) {
			if (!gc.isInternal()) {
				code.addNoIndent(new CodeSnippet("public final GraphClass "
						+ gc.getVariableName() + ";"));
			}
		}
		for (VertexClass vc : schema.getVertexClassesInTopologicalOrder()) {
			code.addNoIndent(new CodeSnippet("public final VertexClass "
					+ vc.getVariableName() + ";"));
		}
		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder()) {
			if (!ec.isInternal()) {
				code
						.addNoIndent(new CodeSnippet(
								"public final "
										+ (ec instanceof CompositionClass ? "Composition"
												: ec instanceof AggregationClass ? "Aggregation"
														: "Edge") + "Class "
										+ ec.getVariableName() + ";"));
			}
		}
		return code;
	}

	private CodeBlock createEdgeClasses(GraphClass gc) {
		CodeList code = new CodeList();
		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder()) {
			if (!ec.isInternal() && ec.getGraphClass() == gc) {
				code.addNoIndent(createEdgeClass(ec));
			}
		}
		return code;
	}

	private CodeBlock createEdgeClass(EdgeClass ec) {
		CodeList code = new CodeList();
		if (ec instanceof CompositionClass) {
			addImports("#jgSchemaPackage#.CompositionClass");
			code.setVariable("ecType", "CompositionClass");
			code.setVariable("aggregateFrom", ((CompositionClass) ec)
					.isAggregateFrom() ? ", true" : ", false");
		} else if (ec instanceof AggregationClass) {
			addImports("#jgSchemaPackage#.AggregationClass");
			code.setVariable("ecType", "AggregationClass");
			code.setVariable("aggregateFrom", ((AggregationClass) ec)
					.isAggregateFrom() ? ", true" : ", false");
		} else {
			addImports("#jgSchemaPackage#.EdgeClass");
			code.setVariable("ecType", "EdgeClass");
			code.setVariable("aggregateFrom", "");
		}

		code.setVariable("ecName", ec.getQualifiedName());
		code.setVariable("schemaVariable", ec.getVariableName());
		code.setVariable("ecVariable", "ec");
		code.setVariable("aecVariable", "ec");
		code.setVariable("ecAbstract", ec.isAbstract() ? "true" : "false");
		code.setVariable("fromClass", ec.getFrom().getVariableName());
		code.setVariable("fromRole", ec.getFromRolename());
		code.setVariable("toClass", ec.getTo().getVariableName());
		code.setVariable("toRole", ec.getToRolename());

		code.setVariable("fromPart", "#fromClass#, " + ec.getFromMin() + ", "
				+ ec.getFromMax() + ", \"#fromRole#\"");
		code.setVariable("toPart", "#toClass#, " + ec.getToMin() + ", "
				+ ec.getToMax() + ", \"#toRole#\"");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"{",
						"\t#ecType# #ecVariable# = #schemaVariable# = #gcVariable#.create#ecType#(new QualifiedName(\"#ecName#\"),",
						"\t\t#fromPart##aggregateFrom#,", "\t\t#toPart#);",
						"\t#ecVariable#.setAbstract(#ecAbstract#);"));
		for (String redefinedFromRole : ec.getRedefinedFromRoles()) {
			CodeSnippet s = new CodeSnippet(
					"#ecVariable#.redefineFromRole(\"#redefinedFromRole#\");");
			s.setVariable("redefinedFromRole", redefinedFromRole);
			code.add(s);
		}

		for (String redefinedToRole : ec.getRedefinedToRoles()) {
			CodeSnippet s = new CodeSnippet(
					"#ecVariable#.redefineToRole(\"#redefinedToRole#\");");
			s.setVariable("redefinedToRole", redefinedToRole);
			code.add(s);
		}

		for (AttributedElementClass superClass : ec.getDirectSuperClasses()) {
			if (superClass.isInternal()) {
				continue;
			}
			CodeSnippet s = new CodeSnippet(
					"#ecVariable#.addSuperClass(#superClassName#);");
			s.setVariable("superClassName", superClass.getVariableName());
			code.add(s);
		}
		code.add(createAttributes(ec));
		code.add(createConstraints(ec));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createVertexClasses(GraphClass gc) {
		CodeList code = new CodeList();
		for (VertexClass vc : schema.getVertexClassesInTopologicalOrder()) {
			if (!vc.isInternal() && vc.getGraphClass() == gc) {
				code.addNoIndent(createVertexClass(vc));
			}
		}
		return code;
	}

	private CodeBlock createVertexClass(VertexClass vc) {
		CodeList code = new CodeList();
		addImports("#jgSchemaPackage#.VertexClass");
		code.setVariable("vcName", vc.getQualifiedName());
		code.setVariable("vcVariable", "vc");
		code.setVariable("aecVariable", "vc");
		code.setVariable("schemaVariable", vc.getVariableName());
		code.setVariable("vcAbstract", vc.isAbstract() ? "true" : "false");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"{",
						"\tVertexClass #vcVariable# = #schemaVariable# = #gcVariable#.createVertexClass(new QualifiedName(\"#vcName#\"));",
						"\t#vcVariable#.setAbstract(#vcAbstract#);"));
		for (AttributedElementClass superClass : vc.getDirectSuperClasses()) {
			if (superClass.isInternal()) {
				continue;
			}
			CodeSnippet s = new CodeSnippet(
					"#vcVariable#.addSuperClass(#superClassName#);");
			s.setVariable("superClassName", superClass.getVariableName());
			code.add(s);
		}
		code.add(createAttributes(vc));
		code.add(createConstraints(vc));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createAttributes(AttributedElementClass aec) {
		CodeList code = new CodeList();
		for (Attribute attr : aec.getOwnAttributeList()) {
			CodeSnippet s = new CodeSnippet(
					false,
					"#aecVariable#.addAttribute(createAttribute(\"#attrName#\", getDomain(\"#domainName#\")));");
			s.setVariable("attrName", attr.getName());
			s.setVariable("domainName", attr.getDomain().getQualifiedName());
			code.addNoIndent(s);
		}
		return code;
	}

	private CodeBlock createConstraints(AttributedElementClass aec) {
		CodeList code = new CodeList();
		for (Constraint constraint : aec.getConstraints()) {
			code
					.addNoIndent(new CodeSnippet(
							false,
							"#aecVariable#.addConstraint(new ConstraintImpl(\""
									+ CodeGenerator.stringQuote(constraint
											.getMessage())
									+ "\", \""
									+ CodeGenerator.stringQuote(constraint
											.getPredicate())
									+ "\", "
									+ ((constraint.getOffendingElementsQuery() != null) ? "\""
											+ CodeGenerator
													.stringQuote(constraint
															.getOffendingElementsQuery())
											+ "\""
											: "null") + "));"));
		}
		return code;
	}

	private CodeBlock createEnumDomains() {
		CodeList code = new CodeList();
		for (EnumDomain dom : schema.getEnumDomains()) {
			CodeSnippet s = new CodeSnippet(true);
			s.setVariable("domName", dom.getQualifiedName());
			code.addNoIndent(s);
			addImports("#jgSchemaPackage#.EnumDomain");
			addImports("#jgSchemaPackage#.QualifiedName");
			s
					.add("{",
							"\tEnumDomain dom = createEnumDomain(new QualifiedName(\"#domName#\"));");
			for (String c : dom.getConsts()) {
				s.add("\tdom.addConst(\"" + c + "\");");
			}
			s.add("}");
		}
		return code;
	}

	private CodeBlock createCompositeDomains() {
		CodeList code = new CodeList();
		for (CompositeDomain dom : schema
				.getCompositeDomainsInTopologicalOrder()) {
			CodeSnippet s = new CodeSnippet(true);
			s.setVariable("domName", dom.getQualifiedName());
			code.addNoIndent(s);
			if (dom instanceof ListDomain) {
				s.setVariable("componentDomainName", ((ListDomain) dom)
						.getBaseDomain().getQualifiedName());
				s
						.add("createListDomain(getDomain(\"#componentDomainName#\"));");
			} else if (dom instanceof SetDomain) {
				s.setVariable("componentDomainName", ((SetDomain) dom)
						.getBaseDomain().getQualifiedName());
				s.add("createSetDomain(getDomain(\"#componentDomainName#\"));");
			} else if (dom instanceof MapDomain) {
				MapDomain mapDom = (MapDomain) dom;
				s.setVariable("keyDomainName", mapDom.getKeyDomain()
						.getQualifiedName());
				s.setVariable("valueDomainName", mapDom.getValueDomain()
						.getQualifiedName());
				s
						.add("createMapDomain(getDomain(\"#keyDomainName#\"), getDomain(\"#valueDomainName#\"));");
			} else if (dom instanceof RecordDomain) {
				addImports("#jgSchemaPackage#.RecordDomain");
				addImports("#jgSchemaPackage#.QualifiedName");
				s
						.add("{",
								"\tRecordDomain dom = createRecordDomain(new QualifiedName(\"#domName#\"));");
				RecordDomain rd = (RecordDomain) dom;
				for (String cName : rd.getComponents().keySet()) {
					s.add("\tdom.addComponent(\"" + cName + "\", getDomain(\""
							+ rd.getComponents().get(cName).getQualifiedName()
							+ "\"));");
				}
				s.add("}");
			} else {
				// never reachable
				throw new RuntimeException("FIXME!");
			}
		}
		return code;
	}
}
