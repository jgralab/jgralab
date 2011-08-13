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

import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class RecordCodeGenerator extends CodeGenerator {

	/**
	 * The RecordDomain to create code for
	 */
	protected RecordDomain recordDomain;

	/**
	 * Creates a new RecordCodeGenerator which creates code for the given
	 * recordDomain object
	 */
	public RecordCodeGenerator(RecordDomain recordDomain,
			String schemaPackageName, CodeGeneratorConfiguration config) {
		super(schemaPackageName, recordDomain.getPackageName(), config);
		rootBlock.setVariable("simpleClassName", recordDomain.getSimpleName());
		rootBlock.setVariable("isClassOnly", "true");
		this.recordDomain = recordDomain;
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = new CodeList();
		if (currentCycle.isClassOnly()) {
			code.add(createRecordComponents());
			code.add(createFieldConstructor());
			code.add(createMapConstructor());
			code.add(createGraphIOConstructor());
			code.add(createGetterMethods());
			code.add(createGenericGetter());
			code.add(createToStringMethod());
			code.add(createReadComponentsMethod());
			code.add(createWriteComponentsMethod());
			code.add(createEqualsMethod());
		}
		return code;
	}

	private CodeBlock createGraphIOConstructor() {
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		return new CodeSnippet(
				true,
				"public #simpleClassName#(GraphIO io) throws GraphIOException {",
				"\treadComponentValues(io);", "}");
	}

	private CodeBlock createFieldConstructor() {
		CodeList code = new CodeList();
		StringBuilder sb = new StringBuilder();
		CodeSnippet header = null;
		header = new CodeSnippet(true, "public #simpleClassName#(#fields#) {");

		code.addNoIndent(header);

		String delim = "";
		for (RecordComponent rdc : recordDomain.getComponents()) {
			sb.append(delim);
			delim = ", ";
			sb.append(rdc.getDomain().getJavaAttributeImplementationTypeName(
					schemaRootPackageName));
			sb.append(" _");
			sb.append(rdc.getName());

			CodeBlock assign = null;
			assign = new CodeSnippet("this._#name# = _#name#;");
			assign.setVariable("name", rdc.getName());
			code.add(assign);
		}
		header.setVariable("fields", sb.toString());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createMapConstructor() {
		CodeList code = new CodeList();
		code.setVariable("rcname", recordDomain.getQualifiedName());

		code.addNoIndent(new CodeSnippet(true,
				"public #simpleClassName#(java.util.Map<String, Object> componentValues) {"));
		code.add(new CodeSnippet(
				"for (String comp: componentValues.keySet()) {"));
		for (RecordComponent rdc : recordDomain.getComponents()) {
			CodeBlock assign = new CodeSnippet(
					"\tif (comp.equals(\"#name#\")) {",
					"\t\t_#name# = (#cls#)componentValues.get(comp);",
					"\t\tcontinue;", "\t}");
			assign.setVariable("name", rdc.getName());
			assign.setVariable("cls",
					rdc.getDomain().getJavaClassName(schemaRootPackageName));
			code.add(assign);
		}
		code.add(new CodeSnippet(
				"\tthrow new NoSuchAttributeException(\"#rcname# doesn't contain an attribute '\" + comp + \"'\");",
				"}"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createEqualsMethod() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(true,
				"public boolean equals(Object o) {"));
		code.add(new CodeSnippet(
				"if (o == null || !(o instanceof #simpleClassName#)) {",
				"\treturn false;", "}",
				"#simpleClassName# record = (#simpleClassName#) o;"));

		CodeSnippet codeSnippet;
		for (RecordComponent entry : recordDomain.getComponents()) {
			codeSnippet = new CodeSnippet(true);
			if (entry.getDomain().isComposite()) {
				codeSnippet.add("\tif(!(_#name#.equals(record._#name#)))");
				codeSnippet.add("\t\treturn false;");
			} else {
				codeSnippet.add("\tif(_#name# != record._#name#)");
				codeSnippet.add("\t\treturn false;");
			}
			code.addNoIndent(codeSnippet);
			codeSnippet.setVariable("name", entry.getName());
		}
		code.add(new CodeSnippet("\n\t\treturn true;"));
		code.addNoIndent(new CodeSnippet("}\n"));
		return code;
	}

	@Override
	protected CodeBlock createHeader() {
		CodeSnippet code = new CodeSnippet(true);
		if (currentCycle.isClassOnly()) {
			addImports("de.uni_koblenz.jgralab.NoSuchAttributeException");
			code.add("public class #simpleClassName# implements de.uni_koblenz.jgralab.Record {");
		}
		return code;
	}

	/**
	 * Getter-methods for fields needed for transaction support.
	 * 
	 * @return
	 */
	protected CodeBlock createGetterMethods() {
		CodeList code = new CodeList();
		for (RecordComponent rdc : recordDomain.getComponents()) {
			CodeSnippet getterCode = new CodeSnippet(true);
			getterCode.setVariable("name", rdc.getName());
			getterCode.setVariable("isOrGet",
					rdc.getDomain().getJavaClassName(schemaRootPackageName)
							.equals("Boolean") ? "is" : "get");
			getterCode.setVariable(
					"type",
					rdc.getDomain().getJavaAttributeImplementationTypeName(
							schemaRootPackageName));

			getterCode.setVariable(
					"ctype",
					rdc.getDomain().getJavaAttributeImplementationTypeName(
							schemaRootPackageName));
			getterCode.add("public #type# #isOrGet#_#name#() {");
			getterCode.add("\treturn _#name#;");
			getterCode.add("}");
			code.addNoIndent(getterCode);
		}
		return code;
	}

	private CodeBlock createGenericGetter() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(false, "@Override"));
		code.addNoIndent(new CodeSnippet(false,
				"public Object getComponent(String name) {"));

		for (RecordComponent rdc : recordDomain.getComponents()) {
			CodeBlock assign = null;
			if (currentCycle.isTransImpl()) {
				assign = new CodeSnippet("if (name.equals(\"#name#\")) {",
						"\treturn #isOrGet#_#name#();", "}");
			} else {
				assign = new CodeSnippet("if (name.equals(\"#name#\")) {",
						"\treturn this._#name#;", "}");
			}

			assign.setVariable("name", rdc.getName());
			assign.setVariable("cname",
					rdc.getDomain().getJavaClassName(schemaRootPackageName));
			assign.setVariable("isOrGet", rdc.getDomain() == rdc.getDomain()
					.getSchema().getBooleanDomain() ? "is" : "get");
			code.add(assign);
		}
		code.add(new CodeSnippet(
				"throw new NoSuchAttributeException(\"#rcname# doesn't contain an attribute \" + name);"));
		code.addNoIndent(new CodeSnippet("}"));
		code.setVariable("rcname", recordDomain.getQualifiedName());
		return code;
	}

	private CodeBlock createReadComponentsMethod() {
		CodeList code = new CodeList();
		// abstract class (or better use interface?)
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code.addNoIndent(new CodeSnippet(true,
				"private void readComponentValues(GraphIO io) throws GraphIOException {"));

		code.add(new CodeSnippet("io.match(\"(\");"));
		for (RecordComponent c : recordDomain.getComponents()) {
			if (currentCycle.isTransImpl()) {
				code.add(c.getDomain().getTransactionReadMethod(
						schemaRootPackageName, "tmp_" + c.getName(), "io"));
				CodeSnippet cs = new CodeSnippet(
						"set_#key#((#typeName#) tmp_#key#);");

				cs.setVariable("key", c.getName());
				cs.setVariable(
						"typeName",
						c.getDomain()
								.getTransactionJavaAttributeImplementationTypeName(
										schemaRootPackageName));
				code.add(cs);
			} else {
				code.add(c.getDomain().getReadMethod(schemaRootPackageName,
						"_" + c.getName(), "io"));
			}
		}
		code.add(new CodeSnippet("io.match(\")\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createWriteComponentsMethod() {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException",
				"java.io.IOException");
		code.addNoIndent(new CodeSnippet(
				true,
				"@Override",
				"public void writeComponentValues(GraphIO io) throws IOException, GraphIOException {",
				"\tio.writeSpace();", "\tio.write(\"(\");", "\tio.noSpace();"));
		for (RecordComponent c : recordDomain.getComponents()) {
			String isOrGet = c.getDomain() instanceof BooleanDomain ? "is"
					: "get";
			code.add(c.getDomain().getWriteMethod(schemaRootPackageName,
					isOrGet + "_" + c.getName() + "()", "io"));
		}
		code.addNoIndent(new CodeSnippet("\tio.write(\")\");", "}"));
		return code;
	}

	private CodeBlock createRecordComponents() {
		CodeList code = new CodeList();
		for (RecordComponent rdc : recordDomain.getComponents()) {
			Domain dom = rdc.getDomain();
			CodeSnippet s = new CodeSnippet(true, "private #type# _#field#;");
			s.setVariable("field", rdc.getName());
			s.setVariable(
					"type",
					dom.getJavaAttributeImplementationTypeName(schemaRootPackageName));
			code.addNoIndent(s);
		}
		return code;
	}

	/**
	 * Creates the toString()-method for this record domain
	 */
	private CodeBlock createToStringMethod() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(true, "public String toString() {",
				"\tStringBuilder sb = new StringBuilder();"));
		String delim = "[";
		for (RecordComponent c : recordDomain.getComponents()) {
			CodeSnippet s = new CodeSnippet("sb.append(\"#delim#\");",
					"sb.append(\"#key#\");", "sb.append(\"=\");",
					"sb.append(#isOrGet#_#key#()#toString#);");
			Domain domain = c.getDomain();
			s.setVariable(
					"isOrGet",
					domain.getJavaClassName(schemaRootPackageName).equals(
							"Boolean") ? "is" : "get");
			s.setVariable("delim", delim);
			s.setVariable("key", c.getName());
			s.setVariable("toString", domain.isComposite() ? ".toString()" : "");
			code.add(s);
			delim = ", ";
		}
		code.addNoIndent(new CodeSnippet("\tsb.append(\"]\");",
				"\treturn sb.toString();", "}"));
		return code;
	}
}
