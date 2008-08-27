/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import java.util.Map.Entry;

import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.RecordDomain;

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
			String schemaPackageName, String implementationName) {
		super(schemaPackageName, recordDomain.getPackageName());
		rootBlock.setVariable("simpleClassName", recordDomain.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", null);
		rootBlock.setVariable("isClassOnly", "true");
		this.recordDomain = recordDomain;
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = new CodeList();
		code.add(createRecordComponents());
		code.add(createFieldConstructor());
		code.add(createMapConstructor());
		code.add(createToStringMethod());
		code.add(createReadComponentsMethod());
		code.add(createWriteComponentsMethod());
		return code;
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		return new CodeSnippet(true, "public class #simpleClassName# {");
	}

	private CodeBlock createFieldConstructor() {
		CodeList code = new CodeList();
		StringBuilder sb = new StringBuilder();
		CodeSnippet header = new CodeSnippet(true,
				"public #simpleClassName#(#fields#) {");
		code.addNoIndent(header);
		String delim = "";
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			sb.append(delim);
			delim = ", ";
			sb.append(rdc.getValue().getJavaAttributeImplementationTypeName(
					schemaRootPackageName));
			sb.append(" ");
			sb.append(rdc.getKey());

			CodeBlock assign = new CodeSnippet("this.#name# = #name#;");
			assign.setVariable("name", rdc.getKey());
			code.add(assign);
		}
		header.setVariable("fields", sb.toString());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createMapConstructor() {
		CodeList code = new CodeList();
		CodeSnippet suppress = new CodeSnippet(true,
				"@SuppressWarnings(\"unchecked\")");
		CodeSnippet header = new CodeSnippet(false,
				"public #simpleClassName#(java.util.Map<String, Object> fields) {");
		code.addNoIndent(suppress);
		code.addNoIndent(header);
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			CodeBlock assign = new CodeSnippet("this.#name# = ("
					+ rdc.getValue().getJavaClassName(schemaRootPackageName)
					+ ")fields.get(\"#name#\");");
			assign.setVariable("name", rdc.getKey());
			code.add(assign);
		}
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createReadComponentsMethod() {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code
				.addNoIndent(new CodeSnippet(true,
						"public #simpleClassName#(GraphIO io) throws GraphIOException {"));
		code.add(new CodeSnippet("io.match(\"(\");"));
		for (Entry<String, Domain> c : recordDomain.getComponents().entrySet()) {
			code.add(c.getValue().getReadMethod(schemaRootPackageName,
					c.getKey(), "io"));
		}
		code.add(new CodeSnippet("io.match(\")\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createWriteComponentsMethod() {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException",
				"java.io.IOException");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"public void writeComponentValues(GraphIO io) throws IOException, GraphIOException {",
						"\tio.writeSpace();", "\tio.write(\"(\");",
						"\tio.noSpace();"));

		for (Entry<String, Domain> c : recordDomain.getComponents().entrySet()) {
			code.add(c.getValue().getWriteMethod(schemaRootPackageName,
					c.getKey(), "io"));
		}

		code.addNoIndent(new CodeSnippet("\tio.write(\")\");", "}"));
		return code;
	}

	private CodeBlock createRecordComponents() {
		CodeList code = new CodeList();
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			CodeSnippet s = new CodeSnippet(true, "public #type# #field#;");
			s.setVariable("type", rdc.getValue()
					.getJavaAttributeImplementationTypeName(
							schemaRootPackageName));
			s.setVariable("field", rdc.getKey());
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
		for (String key : recordDomain.getComponents().keySet()) {
			CodeSnippet s = new CodeSnippet("sb.append(\"#delim#\");",
					"sb.append(\"#key#\");", "sb.append(\" = \");",
					"sb.append(#key##toString#);");

			Domain domain = recordDomain.getComponents().get(key);
			s.setVariable("delim", delim);
			s.setVariable("key", key);
			s
					.setVariable("toString",
							domain.isComposite() ? ".toString()" : "");
			code.add(s);
			delim = ", ";
		}
		code.addNoIndent(new CodeSnippet("\tsb.append(\"]\");",
				"\treturn sb.toString();", "}"));
		return code;
	}

}
