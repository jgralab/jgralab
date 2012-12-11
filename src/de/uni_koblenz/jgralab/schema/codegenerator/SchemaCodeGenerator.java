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

import java.util.List;
import java.util.Stack;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
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

	private final Schema schema;

	/**
	 * Creates a new SchemaCodeGenerator which creates code for the given schema
	 * 
	 * @param schema
	 *            the schema to create the code for
	 * @param schemaPackageName
	 *            the package the schema is located in
	 * @param config
	 *            a CodeGenaratorConfiguration specifying the required variants
	 */
	public SchemaCodeGenerator(Schema schema, String schemaPackageName,
			CodeGeneratorConfiguration config) {
		super(schemaPackageName, "", config);
		this.schema = schema;

		rootBlock.setVariable("simpleClassName", schema.getName());
		rootBlock.setVariable("baseClassName", "SchemaImpl");
		rootBlock.setVariable("isClassOnly", "true");
		rootBlock.setVariable("gcName", schema.getGraphClass()
				.getQualifiedName());
		rootBlock.setVariable("gcCamelName", camelCase(schema.getGraphClass()
				.getQualifiedName()));
		rootBlock.setVariable("gcImplName", schema.getGraphClass()
				.getQualifiedName() + "Impl");
	}

	@Override
	protected CodeBlock createHeader() {
		addImports("#jgSchemaImplPackage#.#baseClassName#");
		addImports("#jgSchemaPackage#.VertexClass");
		addImports("#jgSchemaPackage#.EdgeClass");
		addImports("java.lang.ref.WeakReference");
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
	protected CodeBlock createBody() {
		CodeList code = new CodeList();
		if (currentCycle.isClassOnly()) {
			code.add(createConstructor());
			code.add(createGetDefaultGraphFactoryMethod());
			code.add(createGraphFactoryMethods());
			code.add(createReopenMethod());
		}
		return code;
	}

	private CodeBlock createGetDefaultGraphFactoryMethod() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(
				true,
				"@Override",
				"public #jgPackage#.GraphFactory createDefaultGraphFactory(#jgPackage#.ImplementationType implementationType) {"));
		code.add(new CodeSnippet("switch(implementationType) {"));
		code.add(new CodeSnippet("\tcase GENERIC:",
				"\t\treturn new #jgImplPackage#.generic.GenericGraphFactoryImpl(this);"));
		code.add(new CodeSnippet("\tcase STANDARD:",
				"\t\treturn new #schemaImplStdPackage#.#gcCamelName#FactoryImpl();"));
		code.add(new CodeSnippet("\tcase DISKV2:",
				"\t\treturn new #schemaImplDiskv2Package#.#gcCamelName#FactoryImpl();"));
		code.add(new CodeSnippet(
				"}",
				"throw new UnsupportedOperationException(\"No \" + implementationType + \" support compiled.\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createGraphFactoryMethods() {
		addImports("#jgPackage#.GraphIO",
				"#jgPackage#.exception.GraphIOException");
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(
				true,
				"/**",
				" * Creates a new #gcName# graph.",
				"*/",
				"public #gcName# create#gcCamelName#(#jgPackage#.ImplementationType implType) {",
				"\treturn create#gcCamelName#(implType, null, 100, 100);", "}"));

		code.addNoIndent(new CodeSnippet(
				true,
				"/**",
				" * Creates a new #gcName# graph with initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
				" *",
				" * @param vMax initial vertex count",
				" * @param eMax initial edge count",
				"*/",
				"public #gcName# create#gcCamelName#(#jgPackage#.ImplementationType implType, String id, int vMax, int eMax) {",
				"\t#jgPackage#.GraphFactory factory = createDefaultGraphFactory(implType);",
				"\treturn factory.createGraph(getGraphClass(), id, vMax, eMax);",
				"}"));

		code.addNoIndent(new CodeSnippet(
				true,
				"/**",
				" * Creates a new #gcName# graph.",
				"*/",
				"public #gcName# create#gcCamelName#(#jgPackage#.GraphFactory factory) {",
				"\treturn factory.createGraph(getGraphClass(), null, 100, 100);",
				"}"));

		code.addNoIndent(new CodeSnippet(
				true,
				"/**",
				" * Creates a new #gcName# graph.",
				"*/",
				"public #gcName# create#gcCamelName#(#jgPackage#.GraphFactory factory, String id, int vMax, int eMax) {",
				"\treturn factory.createGraph(getGraphClass(), id, vMax, eMax);",
				"}"));

		// ---- file handling methods ----
		code.addNoIndent(new CodeSnippet(
				true,
				"public #gcName# load#gcCamelName#(String filename) throws GraphIOException {",
				"\t#jgPackage#.GraphFactory factory = createDefaultGraphFactory(#jgPackage#.ImplementationType.STANDARD);",
				"\treturn load#gcCamelName#(filename, factory, null);", "}"));

		code.addNoIndent(new CodeSnippet(
				true,
				"public #gcName# load#gcCamelName#(String filename, #jgPackage#.ProgressFunction pf) throws GraphIOException {",
				"\t#jgPackage#.GraphFactory factory = createDefaultGraphFactory(#jgPackage#.ImplementationType.STANDARD);",
				"\treturn load#gcCamelName#(filename, factory, pf);", "}"));

		code.addNoIndent(new CodeSnippet(
				true,
				"public #gcName# load#gcCamelName#(String filename, #jgPackage#.ImplementationType implType) throws GraphIOException {",
				"\t#jgPackage#.GraphFactory factory = createDefaultGraphFactory(implType);",
				"\treturn load#gcCamelName#(filename, factory, null);", "}"));

		code.addNoIndent(new CodeSnippet(
				true,
				"",
				"public #gcName# load#gcCamelName#(String filename, #jgPackage#.ImplementationType implType, #jgPackage#.ProgressFunction pf) throws GraphIOException {",
				"\t#jgPackage#.GraphFactory factory = createDefaultGraphFactory(implType);",
				"\treturn load#gcCamelName#(filename, factory, pf);", "}"));

		code.addNoIndent(new CodeSnippet(
				true,
				"public #gcName# load#gcCamelName#(String filename, #jgPackage#.GraphFactory factory) throws GraphIOException {",
				"\treturn GraphIO.loadGraphFromFile(filename, factory, null);",
				"}"));

		code.addNoIndent(new CodeSnippet(
				true,
				"public #gcName# load#gcCamelName#(String filename, #jgPackage#.GraphFactory factory, #jgPackage#.ProgressFunction pf) throws GraphIOException {",
				"\treturn GraphIO.loadGraphFromFile(filename, factory, pf);",
				"}"));

		return code;
	}

	private CodeBlock createConstructor() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(
				true,
				"/**",
				" * reference to the singleton instance",
				" */",
				"static WeakReference<#simpleClassName#> theInstance = new WeakReference<#simpleClassName#>(null);",
				"",
				"/**",
				" * @return the singleton instance of #simpleClassName#",
				" */",
				"public static synchronized #simpleClassName# instance() {",
				"\t#simpleClassName# s = theInstance.get();",
				"\tif (s != null) {",
				"\t\treturn s;",
				"\t}",
				"\ts = new #simpleClassName#();",
				"\ttheInstance = new WeakReference<#simpleClassName#>(s);",
				"\treturn s;",
				"}",
				"",
				"/**",
				" * Creates a #simpleClassName# and builds its schema classes.",
				" * This constructor is private. Use the <code>instance()</code> method",
				" * to access the schema.", " */",
				"private #simpleClassName#() {",
				"\tsuper(\"#simpleClassName#\", \"#schemaPackage#\");"));

		code.add(createEnumDomains());
		code.add(createCompositeDomains());
		code.add(createGraphClass());
		code.add(createPackageComments());
		code.addNoIndent(new CodeSnippet(true, "\tfinish();", "}"));
		return code;
	}

	private CodeBlock createPackageComments() {
		CodeList code = new CodeList();
		Package pkg = schema.getDefaultPackage();
		Stack<Package> s = new Stack<Package>();
		s.push(pkg);
		boolean hasComment = false;
		while (!s.isEmpty()) {
			pkg = s.pop();
			for (Package sub : pkg.getSubPackages()) {
				s.push(sub);
			}
			List<String> comments = pkg.getComments();
			if (comments.isEmpty()) {
				continue;
			}
			if (!hasComment) {
				code.addNoIndent(new CodeSnippet(true, "{"));
				hasComment = true;
			}
			for (String comment : comments) {
				code.add(new CodeSnippet("getPackage(\""
						+ pkg.getQualifiedName() + "\").addComment(\""
						+ stringQuote(comment) + "\");"));
			}
		}
		if (hasComment) {
			code.addNoIndent(new CodeSnippet(false, "}"));
		}
		return code;
	}

	private CodeBlock createGraphClass() {
		GraphClass gc = schema.getGraphClass();
		CodeList code = new CodeList();
		addImports("#jgSchemaPackage#.GraphClass");
		code.setVariable("gcVariable", "gc");
		code.setVariable("aecVariable", "gc");
		code.setVariable("gcAbstract", gc.isAbstract() ? "true" : "false");
		code.addNoIndent(new CodeSnippet(true, "{",
				"\tGraphClass #gcVariable# = createGraphClass(\"#gcName#\");"));
		code.addNoIndent(createAttributes(gc));
		code.addNoIndent(createConstraints(gc));
		code.addNoIndent(createComments("gc", gc));
		code.addNoIndent(createVertexClasses(gc));
		code.addNoIndent(createEdgeClasses(gc));
		code.addNoIndent(new CodeSnippet(false, "}"));
		return code;
	}

	private CodeBlock createComments(String variableName, NamedElement ne) {
		CodeList code = new CodeList();
		code.setVariable("namedElement", variableName);
		for (String comment : ne.getComments()) {
			code.addNoIndent(new CodeSnippet("#namedElement#.addComment("
					+ GraphIO.toUtfString(comment) + ");"));
		}
		return code;
	}

	private CodeBlock createEdgeClasses(GraphClass gc) {
		CodeList code = new CodeList();
		for (EdgeClass ec : schema.getGraphClass().getEdgeClasses()) {
			code.addNoIndent(createEdgeClass(ec));
		}
		return code;
	}

	private CodeBlock createEdgeClass(EdgeClass ec) {
		CodeList code = new CodeList();
		addImports("#jgSchemaPackage#.EdgeClass");
		code.setVariable("ecType", "EdgeClass");

		code.setVariable("ecName", ec.getQualifiedName());
		code.setVariable("aecVariable", gecVarName(ec));
		code.setVariable("ecAbstract", ec.isAbstract() ? "true" : "false");
		code.setVariable("fromClass", gecVarName(ec.getFrom().getVertexClass()));
		code.setVariable("fromRole", ec.getFrom().getRolename());
		code.setVariable("toClass", gecVarName(ec.getTo().getVertexClass()));
		code.setVariable("toRole", ec.getTo().getRolename());
		code.setVariable("toAggregation",
				AggregationKind.class.getCanonicalName() + "."
						+ ec.getTo().getAggregationKind().toString());
		code.setVariable("fromAggregation",
				AggregationKind.class.getCanonicalName() + "."
						+ ec.getFrom().getAggregationKind().toString());
		code.setVariable("fromPart", "#fromClass#, " + ec.getFrom().getMin()
				+ ", " + ec.getFrom().getMax() + ", \"#fromRole#\""
				+ ", #fromAggregation#");
		code.setVariable("toPart", "#toClass#, " + ec.getTo().getMin() + ", "
				+ ec.getTo().getMax() + ", \"#toRole#\"" + ", #toAggregation#");
		code.addNoIndent(new CodeSnippet(
				true,
				"\t#ecType# #aecVariable# = #gcVariable#.create#ecType#(\"#ecName#\",",
				"\t\t#fromPart#,", "\t\t#toPart#);",
				"\t#aecVariable#.setAbstract(#ecAbstract#);"));

		for (EdgeClass superClass : ec.getDirectSuperClasses()) {
			CodeSnippet s = new CodeSnippet(
					"#aecVariable#.addSuperClass(#superClassName#);");
			s.setVariable("superClassName", gecVarName(superClass));
			code.add(s);
		}

		code.add(createAttributes(ec));
		code.add(createConstraints(ec));
		code.add(createComments(gecVarName(ec), ec));
		return code;
	}

	private CodeBlock createVertexClasses(GraphClass gc) {
		CodeList code = new CodeList();
		for (VertexClass vc : schema.getGraphClass().getVertexClasses()) {
			code.addNoIndent(createVertexClass(vc));
		}
		return code;
	}

	private String gecVarName(GraphElementClass<?, ?> gec) {
		StringBuilder sb = new StringBuilder();
		if (gec instanceof VertexClass) {
			sb.append("vc_");
		} else {
			sb.append("ec_");
		}
		sb.append(gec.getUniqueName());
		return sb.toString();
	}

	private CodeBlock createVertexClass(VertexClass vc) {
		CodeList code = new CodeList();
		code.setVariable("vcName", vc.getQualifiedName());
		code.setVariable("aecVariable", gecVarName(vc));
		code.setVariable("vcAbstract", vc.isAbstract() ? "true" : "false");
		code.addNoIndent(new CodeSnippet(
				true,
				"\tVertexClass #aecVariable# = #gcVariable#.createVertexClass(\"#vcName#\");",
				"\t#aecVariable#.setAbstract(#vcAbstract#);"));
		for (VertexClass superClass : vc.getDirectSuperClasses()) {
			CodeSnippet s = new CodeSnippet(
					"#aecVariable#.addSuperClass(#superClassName#);");
			s.setVariable("superClassName", gecVarName(superClass));
			code.add(s);
		}

		code.add(createAttributes(vc));
		code.add(createConstraints(vc));
		code.add(createComments(gecVarName(vc), vc));
		return code;
	}

	private CodeBlock createAttributes(AttributedElementClass<?, ?> aec) {
		CodeList code = new CodeList();
		List<Attribute> attributes = (aec instanceof GraphElementClass) ? ((GraphElementClass<?, ?>) aec)
				.getOwnAttributeList() : aec.getAttributeList();
		for (Attribute attr : attributes) {
			CodeSnippet s = new CodeSnippet(
					false,
					"#aecVariable#.createAttribute(\"#attrName#\", getDomain(\"#domainName#\"), #defaultValue#);");
			s.setVariable("attrName", attr.getName());
			s.setVariable("domainName", attr.getDomain().getQualifiedName());
			s.setVariable("aecName", aec.getQualifiedName());
			if (attr.getDefaultValueAsString() == null) {
				s.setVariable("defaultValue", "null");
			} else {
				// quote double quotes
				String defaultValue = attr.getDefaultValueAsString()
						.replaceAll("([\\\"])", "\\\\$1");
				// don't confuse code generator with # characters contained in
				// default values
				defaultValue = defaultValue.replaceAll("#", "\\u0023");
				s.setVariable("defaultValue", "\"" + defaultValue + "\"");
			}
			code.addNoIndent(s);
		}
		return code;
	}

	private CodeBlock createConstraints(AttributedElementClass<?, ?> aec) {
		CodeList code = new CodeList();
		for (Constraint constraint : aec.getConstraints()) {
			addImports("#jgSchemaImplPackage#.ConstraintImpl");
			CodeSnippet constraintSnippet = new CodeSnippet(false);
			constraintSnippet
					.add("#aecVariable#.addConstraint("
							+ "new ConstraintImpl(#message#, #predicate#, #offendingElements#));");
			constraintSnippet.setVariable("message", "\""
					+ stringQuote(constraint.getMessage()) + "\"");
			constraintSnippet.setVariable("predicate", "\""
					+ stringQuote(constraint.getPredicate()) + "\"");
			if (constraint.getOffendingElementsQuery() != null) {
				constraintSnippet.setVariable("offendingElements", "\""
						+ stringQuote(constraint.getOffendingElementsQuery())
						+ "\"");
			} else {
				constraintSnippet.setVariable("offendingElements", "null");
			}
			code.addNoIndent(constraintSnippet);
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
			s.add("{", "\tEnumDomain dom = createEnumDomain(\"#domName#\");");
			for (String c : dom.getConsts()) {
				s.add("\tdom.addConst(\"" + c + "\");");
			}
			code.add(createComments("dom", dom));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createCompositeDomains() {
		CodeList code = new CodeList();
		for (CompositeDomain dom : schema.getCompositeDomains()) {
			CodeSnippet s = new CodeSnippet(true);
			s.setVariable("domName", dom.getQualifiedName());
			code.addNoIndent(s);
			if (dom instanceof ListDomain) {
				s.setVariable("componentDomainName", ((ListDomain) dom)
						.getBaseDomain().getQualifiedName());
				s.add("createListDomain(getDomain(\"#componentDomainName#\"));");
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
				s.add("createMapDomain(getDomain(\"#keyDomainName#\"), getDomain(\"#valueDomainName#\"));");
			} else if (dom instanceof RecordDomain) {
				addImports("#jgSchemaPackage#.RecordDomain");
				s.add("{",
						"\tRecordDomain dom = createRecordDomain(\"#domName#\");");
				RecordDomain rd = (RecordDomain) dom;
				for (RecordComponent c : rd.getComponents()) {
					s.add("\tdom.addComponent(\"" + c.getName()
							+ "\", getDomain(\""
							+ c.getDomain().getQualifiedName() + "\"));");
				}
				code.add(createComments("dom", rd));
				code.addNoIndent(new CodeSnippet("}"));
			} else {
				throw new RuntimeException("FIXME!"); // never reachable
			}
		}
		return code;
	}

	private CodeBlock createReopenMethod() {
		CodeSnippet s = new CodeSnippet();
		s.add("",
				"@Override",
				"public boolean reopen() {",
				"\tthrow new UnsupportedOperationException(\"Cannot reopen a compiled Schema.\");",
				"}");
		return s;
	}
}
