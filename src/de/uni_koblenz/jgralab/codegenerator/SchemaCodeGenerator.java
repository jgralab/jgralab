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
	 * @param implementationName
	 *            the special jgralab package name to use
	 */
	public SchemaCodeGenerator(Schema schema, String schemaPackageName,
			String implementationName, CodeGeneratorConfiguration config) {
		super(schemaPackageName, "", config);
		this.schema = schema;

		rootBlock.setVariable("simpleClassName", schema.getName());
		rootBlock.setVariable("simpleImplClassName", schema.getName());
		rootBlock.setVariable("baseClassName", "SchemaImpl");
		rootBlock.setVariable("isAbstractClass", "false");
		rootBlock.setVariable("isClassOnly", "true");
		rootBlock.setVariable("isImplementationClassOnly", "false");
	}

	@Override
	protected CodeBlock createHeader() {
		addImports("#jgSchemaImplPackage#.#baseClassName#");
		addImports("#jgSchemaPackage#.VertexClass");
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
			code.add(createVariables());
			code.add(createConstructor());
			code.add(createGraphFactoryMethod());
		}
		return code;
	}

	@Override
	protected CodeBlock createFooter() {
		CodeList footer = new CodeList();
		// override equals and hashCode methods
		footer.add(new CodeSnippet("", "@Override",
				"public boolean equals(Object o) {",
				"\treturn super.equals(o);", "}"));
		footer.add(new CodeSnippet("", "@Override", "public int hashCode() {",
				"\treturn super.hashCode();", "}"));
		footer.addNoIndent(super.createFooter());
		return footer;
	}

	private CodeBlock createGraphFactoryMethod() {
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
				((config.hasStandardSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraph(#gcCamelName#.class, null, vMax, eMax);"
						: "\tthrow new UnsupportedOperationException(\"No Standard support compiled.\");"),
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
				((config.hasStandardSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraph(#gcCamelName#.class, id, vMax, eMax);"
						: "\tthrow new UnsupportedOperationException(\"No Standard support compiled.\");"),
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph.",
				"*/",
				"public #gcName# create#gcCamelName#() {",
				((config.hasStandardSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraph(#gcCamelName#.class, null);"
						: "\tthrow new UnsupportedOperationException(\"No Standard support compiled.\");"),
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph with the ID <code>id</code>.",
				" *",
				" * @param id the id name of the new graph",
				" */",
				"public #gcName# create#gcCamelName#(String id) {",
				((config.hasStandardSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraph(#gcCamelName#.class, id);"
						: "\tthrow new UnsupportedOperationException(\"No Standard support compiled.\");"),
				"}",
				"",
				// ---- savemem support ----
				// TODO Currently redirect to STD methods. Extension needed?
				"/**",
				" * Creates a new #gcName# graph with savemem support with initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
				" *",
				" * @param vMax initial vertex count",
				" * @param eMax initial edge count",
				"*/",
				"public #gcName# create#gcCamelName#WithSavememSupport(int vMax, int eMax) {",
				((config.hasSavememSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraphWithSavememSupport(#gcCamelName#.class, null, vMax, eMax);"
						: "\tthrow new UnsupportedOperationException(\"No Savemem support compiled.\");"),
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph with savemem support with the ID <code>id</code> initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
				" *",
				" * @param id the id name of the new graph",
				" * @param vMax initial vertex count",
				" * @param eMax initial edge count",
				" */",
				"public #gcName# create#gcCamelName#WithSavememSupport(String id, int vMax, int eMax) {",
				((config.hasSavememSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraphWithSavememSupport(#gcCamelName#.class, id, vMax, eMax);"
						: "\tthrow new UnsupportedOperationException(\"No Savemem support compiled.\");"),
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph.",
				"*/",
				"public #gcName# create#gcCamelName#WithSavememSupport() {",
				((config.hasSavememSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraphWithSavememSupport(#gcCamelName#.class, null);"
						: "\tthrow new UnsupportedOperationException(\"No Savemem support compiled.\");"),
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph with the ID <code>id</code>.",
				" *",
				" * @param id the id name of the new graph",
				" */",
				"public #gcName# create#gcCamelName#WithSavememSupport(String id) {",
				((config.hasSavememSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraphWithSavememSupport(#gcCamelName#.class, id);"
						: "\tthrow new UnsupportedOperationException(\"No Savemem support compiled.\");"),
				"}",
				"",
				// ---- transaction support ----
				"/**",
				" * Creates a new #gcName# graph with transaction support with initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
				" *",
				" * @param vMax initial vertex count",
				" * @param eMax initial edge count",
				"*/",
				"public #gcName# create#gcCamelName#WithTransactionSupport(int vMax, int eMax) {",
				((config.hasTransactionSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraphWithTransactionSupport(#gcCamelName#.class, null, vMax, eMax);"
						: "\tthrow new UnsupportedOperationException(\"No Transaction support compiled.\");"),
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph with transaction support with the ID <code>id</code> initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
				" *",
				" * @param id the id name of the new graph",
				" * @param vMax initial vertex count",
				" * @param eMax initial edge count",
				" */",
				"public #gcName# create#gcCamelName#WithTransactionSupport(String id, int vMax, int eMax) {",
				((config.hasTransactionSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraphWithTransactionSupport(#gcCamelName#.class, id, vMax, eMax);"
						: "\tthrow new UnsupportedOperationException(\"No Transaction support compiled.\");"),
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph.",
				"*/",
				"public #gcName# create#gcCamelName#WithTransactionSupport() {",
				((config.hasTransactionSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraphWithTransactionSupport(#gcCamelName#.class, null);"
						: "\tthrow new UnsupportedOperationException(\"No Transaction support compiled.\");"),
				"}",
				"",
				"/**",
				" * Creates a new #gcName# graph with the ID <code>id</code>.",
				" *",
				" * @param id the id name of the new graph",
				" */",
				"public #gcName# create#gcCamelName#WithTransactionSupport(String id) {",
				((config.hasTransactionSupport()) ? "\treturn (#gcCamelName#) graphFactory.createGraphWithTransactionSupport(#gcCamelName#.class, id);"
						: "\tthrow new UnsupportedOperationException(\"No Transaction support compiled.\");"),
				"}",
				"",
				// ---- file handling methods ----
				"/**",
				" * Loads a #gcName# graph from the file <code>filename</code>.",
				" *",
				" * @param filename the name of the file",
				" * @return the loaded #gcName#",
				" * @throws GraphIOException if the graph cannot be loaded",
				" */",
				"public #gcName# load#gcCamelName#(String filename) throws GraphIOException {",
				((config.hasStandardSupport()) ? "\treturn load#gcCamelName#(filename, null);"
						: "\tthrow new UnsupportedOperationException(\"No Standard support compiled.\");"),
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
				((config.hasStandardSupport()) ? "\tGraph graph = GraphIO.loadGraphFromFileWithStandardSupport(filename, this, pf);\n"
						+ "\tif (!(graph instanceof #gcName#)) {\n"
						+ "\t\tthrow new GraphIOException(\"Graph in file '\" + filename + \"' is not an instance of GraphClass #gcName#\");\n"
						+ "\t}" + "\treturn (#gcName#) graph;\n"
						: "\tthrow new UnsupportedOperationException(\"No Standard support compiled.\");"),
				"}",
				"",
				"/**",
				" * Saves a #gcName# graph to the file <code>filename</code>.",
				" *",
				" * @param #gcName# the graph to save",
				" * @param filename the name of the file",
				" * @throws GraphIOException if the graph cannot be saved",
				" */",
				"",
				"public void save#gcCamelName#(String filename, #gcName# #gcCamelName#) throws GraphIOException {",
				"\tsave#gcCamelName#(filename, #gcCamelName#, null);",
				"}",
				"/**",
				" * Saves a #gcName# graph to the file <code>filename</code>.",
				" *",
				" * @param #gcName# the graph to save",
				" * @param filename the name of the file",
				" * @param pf a progress function to monitor graph loading",
				" * @throws GraphIOException if the graph cannot be saved",
				" */",
				"",
				"public void save#gcCamelName#(String filename, #gcName# #gcCamelName#, ProgressFunction pf) throws GraphIOException {",
				"\tGraphIO.saveGraphToFile(filename, #gcCamelName#, pf);",
				"}",
				"",
				// ---- file handling methods with savemem support ----
				// TODO Currently redirect to STD methods. Extension needed?
				"/**",
				" * Loads a #gcName# graph with savemem support from the file <code>filename</code>.",
				" *",
				" * @param filename the name of the file",
				" * @return the loaded #gcName#",
				" * @throws GraphIOException if the graph cannot be loaded",
				" */",
				"public #gcName# load#gcCamelName#WithSavememSupport(String filename) throws GraphIOException {",
				((config.hasSavememSupport()) ? "\treturn load#gcCamelName#WithSavememSupport(filename, null);"
						: "\tthrow new UnsupportedOperationException(\"No Savemem support compiled.\");"),
				"}",
				"",
				"/**",
				" * Loads a #gcName# graph with savemem support from the file <code>filename</code>.",
				" *",
				" * @param filename the name of the file",
				" * @param pf a progress function to monitor graph loading",
				" * @return the loaded #gcName#",
				" * @throws GraphIOException if the graph cannot be loaded",
				" */",
				"public #gcName# load#gcCamelName#WithSavememSupport(String filename, ProgressFunction pf) throws GraphIOException {",
				((config.hasSavememSupport()) ? "\tGraph graph = GraphIO.loadGraphFromFileWithSavememSupport(filename, pf);\n"
						+ "\tif (!(graph instanceof #gcName#)) {\n"
						+ "\t\tthrow new GraphIOException(\"Graph in file '\" + filename + \"' is not an instance of GraphClass #gcName#\");\n"
						+ "\t}" + "\treturn (#gcName#) graph;"
						: "\tthrow new UnsupportedOperationException(\"No Savemem support compiled.\");"),
				"}",
				// ---- file handling methods with transaction support ----
				"/**",
				" * Loads a #gcName# graph with transaction support from the file <code>filename</code>.",
				" *",
				" * @param filename the name of the file",
				" * @return the loaded #gcName#",
				" * @throws GraphIOException if the graph cannot be loaded",
				" */",
				"public #gcName# load#gcCamelName#WithTransactionSupport(String filename) throws GraphIOException {",
				((config.hasTransactionSupport()) ? "\treturn load#gcCamelName#WithTransactionSupport(filename, null);"
						: "\tthrow new UnsupportedOperationException(\"No Transaction support compiled.\");"),
				"}",
				"",
				"/**",
				" * Loads a #gcName# graph with transaction support from the file <code>filename</code>.",
				" *",
				" * @param filename the name of the file",
				" * @param pf a progress function to monitor graph loading",
				" * @return the loaded #gcName#",
				" * @throws GraphIOException if the graph cannot be loaded",
				" */",
				"public #gcName# load#gcCamelName#WithTransactionSupport(String filename, ProgressFunction pf) throws GraphIOException {",
				((config.hasTransactionSupport()) ? "\tGraph graph = GraphIO.loadGraphFromFileWithTransactionSupport(filename, pf);\n"
						+ "\tif (!(graph instanceof #gcName#)) {\n"
						+ "\t\tthrow new GraphIOException(\"Graph in file '\" + filename + \"' is not an instance of GraphClass #gcName#\");\n"
						+ "\t}" + "\treturn (#gcName#) graph;"
						: "\tthrow new UnsupportedOperationException(\"No Transaction support compiled.\");"),
				"}");
		code.setVariable("gcName", schema.getGraphClass().getQualifiedName());
		code.setVariable("gcCamelName", camelCase(schema.getGraphClass()
				.getQualifiedName()));
		code.setVariable("gcImplName", schema.getGraphClass()
				.getQualifiedName() + "Impl");

		return code;
	}

	private CodeBlock createConstructor() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(
				true,
				"/**",
				" * the weak reference to the singleton instance",
				" */",
				"static WeakReference<#simpleClassName#> theInstance = new WeakReference<#simpleClassName#>(null);",
				"",
				"/**",
				" * @return the singleton instance of #simpleClassName#",
				" */",
				"public static #simpleClassName# instance() {",
				"\t#simpleClassName# s = theInstance.get();",
				"\tif (s != null) {",
				"\t\treturn s;",
				"\t}",
				"\tsynchronized (#simpleClassName#.class) {",
				"\t\ts = theInstance.get();",
				"\t\tif (s != null) {",
				"\t\t\treturn s;",
				"\t\t}",
				"\t\ts = new #simpleClassName#();",
				"\t\ttheInstance = new WeakReference<#simpleClassName#>(s);",
				"\t}",
				"\treturn s;",
				"}",
				"",
				"/**",
				" * Creates a #simpleClassName# and builds its schema classes.",
				" * This constructor is private. Use the <code>instance()</code> method",
				" * to acess the schema.", " */",
				"private #simpleClassName#() {",
				"\tsuper(\"#simpleClassName#\", \"#schemaPackage#\");"));

		code.add(createEnumDomains());
		code.add(createCompositeDomains());
		code.add(createGraphClass());
		code.add(createPackageComments());
		addImports("#schemaPackage#.#simpleClassName#Factory");
		code.add(new CodeSnippet(true,
				"graphFactory = new #simpleClassName#Factory();"));
		code.addNoIndent(new CodeSnippet(true, "}"));

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
			for (Package sub : pkg.getSubPackages().values()) {
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
			if (comments.size() == 1) {
				code.add(new CodeSnippet("getPackage(\""
						+ pkg.getQualifiedName() + "\").addComment(\""
						+ stringQuote(comments.get(0)) + "\");"));
			} else {
				int n = 0;
				code.add(new CodeSnippet("getPackage(\""
						+ pkg.getQualifiedName() + "\").addComment("));
				for (String comment : comments) {
					code.add(new CodeSnippet("\t\"" + stringQuote(comment)
							+ "\"" + (++n == comments.size() ? ");" : ",")));
				}

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
		code.setVariable("gcName", gc.getQualifiedName());
		code.setVariable("gcVariable", "gc");
		code.setVariable("aecVariable", "gc");
		code.setVariable("schemaVariable", gc.getVariableName());
		code.setVariable("gcAbstract", gc.isAbstract() ? "true" : "false");
		code.addNoIndent(new CodeSnippet(
				true,
				"{",
				"\tGraphClass #gcVariable# = #schemaVariable# = createGraphClass(\"#gcName#\");",
				"\t#gcVariable#.setAbstract(#gcAbstract#);"));
		for (AttributedElementClass superClass : gc.getDirectSuperClasses()) {
			if (superClass.isInternal()) {
				continue;
			}
			CodeSnippet s = new CodeSnippet(
					"#gcVariable#.addSuperClass(getGraphClass(\"#superClassName#\"));");
			s.setVariable("superClassName", superClass.getQualifiedName());
			code.add(s);
		}
		code.add(createAttributes(gc));
		code.add(createConstraints(gc));
		code.add(createComments("gc", gc));
		code.add(createVertexClasses(gc));
		code.add(createEdgeClasses(gc));
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

	private CodeBlock createVariables() {
		CodeList code = new CodeList();

		code.addNoIndent(new CodeSnippet("public final GraphClass "
				+ schema.getGraphClass().getVariableName() + ";"));

		for (VertexClass vc : schema.getVertexClassesInTopologicalOrder()) {
			if (!vc.isInternal()) {
				code.addNoIndent(new CodeSnippet("public final VertexClass "
						+ vc.getVariableName() + ";"));
			}
		}
		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder()) {
			if (!ec.isInternal()) {
				code.addNoIndent(new CodeSnippet("public final EdgeClass "
						+ ec.getVariableName() + ";"));
			}
		}
		return code;
	}

	private CodeBlock createEdgeClasses(GraphClass gc) {
		CodeList code = new CodeList();
		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder()) {
			if (!ec.isInternal() && (ec.getGraphClass() == gc)) {
				code.addNoIndent(createEdgeClass(ec));
			}
		}
		return code;
	}

	private CodeBlock createEdgeClass(EdgeClass ec) {
		CodeList code = new CodeList();
		addImports("#jgSchemaPackage#.EdgeClass");
		code.setVariable("ecType", "EdgeClass");

		code.setVariable("ecName", ec.getQualifiedName());
		code.setVariable("schemaVariable", ec.getVariableName());
		code.setVariable("ecVariable", "ec");
		code.setVariable("aecVariable", "ec");
		code.setVariable("ecAbstract", ec.isAbstract() ? "true" : "false");
		code.setVariable("fromClass", ec.getFrom().getVertexClass()
				.getVariableName());
		code.setVariable("fromRole", ec.getFrom().getRolename());
		code.setVariable("toClass", ec.getTo().getVertexClass()
				.getVariableName());
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
				"{",
				"\t#ecType# #ecVariable# = #schemaVariable# = #gcVariable#.create#ecType#(\"#ecName#\",",
				"\t\t#fromPart#,", "\t\t#toPart#);",
				"\t#ecVariable#.setAbstract(#ecAbstract#);"));

		for (AttributedElementClass superClass : ec.getDirectSuperClasses()) {
			if (superClass.isInternal()) {
				continue;
			}
			CodeSnippet s = new CodeSnippet(
					"#ecVariable#.addSuperClass(#superClassName#);");
			s.setVariable("superClassName", superClass.getVariableName());
			code.add(s);
		}

		for (String redefinedFromRole : ec.getFrom().getRedefinedRoles()) {
			CodeSnippet s = new CodeSnippet(
					"#ecVariable#.getFrom().addRedefinedRole(\"#redefinedFromRole#\");");
			s.setVariable("redefinedFromRole", redefinedFromRole);
			code.add(s);
		}

		for (String redefinedToRole : ec.getTo().getRedefinedRoles()) {
			CodeSnippet s = new CodeSnippet(
					"#ecVariable#.getTo().addRedefinedRole(\"#redefinedToRole#\");");
			s.setVariable("redefinedToRole", redefinedToRole);
			code.add(s);
		}

		code.add(createAttributes(ec));
		code.add(createConstraints(ec));
		code.add(createComments("ec", ec));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createVertexClasses(GraphClass gc) {
		CodeList code = new CodeList();
		for (VertexClass vc : schema.getVertexClassesInTopologicalOrder()) {
			if (vc.isInternal()) {
				CodeSnippet s = new CodeSnippet();
				s.setVariable("schemaVariable", vc.getVariableName());
				s.add("@SuppressWarnings(\"unused\")");
				s.add("VertexClass #schemaVariable# = getDefaultVertexClass();");
				code.addNoIndent(s);
			} else if (vc.getGraphClass() == gc) {
				code.addNoIndent(createVertexClass(vc));
			}
		}
		return code;
	}

	private CodeBlock createVertexClass(VertexClass vc) {
		CodeList code = new CodeList();
		code.setVariable("vcName", vc.getQualifiedName());
		code.setVariable("vcVariable", "vc");
		code.setVariable("aecVariable", "vc");
		code.setVariable("schemaVariable", vc.getVariableName());
		code.setVariable("vcAbstract", vc.isAbstract() ? "true" : "false");
		code.addNoIndent(new CodeSnippet(
				true,
				"{",
				"\tVertexClass #vcVariable# = #schemaVariable# = #gcVariable#.createVertexClass(\"#vcName#\");",
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
		code.add(createComments("vc", vc));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createAttributes(AttributedElementClass aec) {
		CodeList code = new CodeList();
		for (Attribute attr : aec.getOwnAttributeList()) {
			CodeSnippet s = new CodeSnippet(
					false,
					"#aecVariable#.addAttribute(createAttribute(\"#attrName#\", getDomain(\"#domainName#\"), getAttributedElementClass(\"#aecName#\"), #defaultValue#));");
			s.setVariable("attrName", attr.getName());
			s.setVariable("domainName", attr.getDomain().getQualifiedName());
			s.setVariable("aecName", aec.getQualifiedName());
			if (attr.getDefaultValueAsString() == null) {
				s.setVariable("defaultValue", "null");
			} else {
				s.setVariable(
						"defaultValue",
						"\""
								+ attr.getDefaultValueAsString().replaceAll(
										"([\\\"])", "\\\\$1") + "\"");
			}
			code.addNoIndent(s);
		}
		return code;
	}

	private CodeBlock createConstraints(AttributedElementClass aec) {
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
		for (CompositeDomain dom : schema
				.getCompositeDomainsInTopologicalOrder()) {
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
				// never reachable
				throw new RuntimeException("FIXME!");
			}
		}
		return code;
	}
}
