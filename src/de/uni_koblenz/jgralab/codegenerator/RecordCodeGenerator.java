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

import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.StringDomain;

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
			code.add(createWriteComponentsMethod());
			code.add(createToPMapMethod());
			code.add(createEqualsMethod());
			code.add(createHashCodeMethod());
		}
		return code;
	}

	private CodeBlock createGraphIOConstructor() {
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(true,
				"public #simpleClassName#(GraphIO io) throws GraphIOException {"));

		code.add(new CodeSnippet("io.match(\"(\");"));
		for (RecordComponent rc : recordDomain.getComponents()) {
			if (currentCycle.isTransImpl()) {
				code.add(rc.getDomain().getTransactionReadMethod(
						schemaRootPackageName, "tmp_" + rc.getName(), "io"));
				CodeSnippet cs = new CodeSnippet(
						"set_#key#((#typeName#) tmp_#key#);");

				cs.setVariable("key", rc.getName());
				cs.setVariable(
						"typeName",
						rc.getDomain()
								.getTransactionJavaAttributeImplementationTypeName(
										schemaRootPackageName));
				code.add(cs);
			} else {
				code.add(rc.getDomain().getReadMethod(schemaRootPackageName,
						"_" + rc.getName(), "io"));
			}
		}
		code.add(new CodeSnippet("io.match(\")\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createFieldConstructor() {
		CodeList code = new CodeList();
		StringBuilder sb = new StringBuilder();
		code.addNoIndent(new CodeSnippet(true,
				"public #simpleClassName#(#fields#) {"));
		String delim = "";
		for (RecordComponent rc : recordDomain.getComponents()) {
			sb.append(delim);
			delim = ", ";
			sb.append(rc.getDomain().getJavaAttributeImplementationTypeName(
					schemaRootPackageName));
			sb.append(" _");
			sb.append(rc.getName());

			CodeBlock assign = null;
			assign = new CodeSnippet("this._#name# = _#name#;");
			assign.setVariable("name", rc.getName());
			code.add(assign);
		}
		code.setVariable("fields", sb.toString());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createMapConstructor() {
		CodeList code = new CodeList();
		addImports("de.uni_koblenz.jgralab.NoSuchAttributeException");
		code.setVariable("rcname", recordDomain.getQualifiedName());

		CodeSnippet suppressUnchecked = new CodeSnippet("");
		code.addNoIndent(suppressUnchecked);
		code.addNoIndent(new CodeSnippet(
				"public #simpleClassName#(java.util.Map<String, Object> componentValues) {"));

		code.add(new CodeSnippet("assert componentValues.size() == "
				+ recordDomain.getComponents().size() + ";"));
		for (RecordComponent rc : recordDomain.getComponents()) {
			if (rc.getDomain().isComposite() && (suppressUnchecked.size() <= 1)) {
				suppressUnchecked.add("@SuppressWarnings(\"unchecked\")");
			}
			CodeBlock assign = new CodeSnippet(
					"assert componentValues.containsKey(\"#name#\");",
					"_#name# = (#cls#)componentValues.get(\"#name#\");");
			assign.setVariable("name", rc.getName());
			assign.setVariable("cls",
					rc.getDomain().getJavaClassName(schemaRootPackageName));
			code.add(assign);
		}
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createToPMapMethod() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(
				"public org.pcollections.PMap<String, Object> toPMap() {"));
		code.add(new CodeSnippet(
				"org.pcollections.PMap<String, Object> m = de.uni_koblenz.jgralab.JGraLab.map();"));
		for (RecordComponent rc : recordDomain.getComponents()) {
			CodeBlock assign = new CodeSnippet(
					"m = m.plus(\"#name#\", _#name#);");
			assign.setVariable("name", rc.getName());
			code.add(assign);
		}
		code.add(new CodeSnippet("return m;"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createHashCodeMethod() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(true, "@Override",
				"public int hashCode() {", "\tint h = 0;"));
		for (RecordComponent rc : recordDomain.getComponents()) {
			CodeSnippet assign = new CodeSnippet();
			code.add(assign);
			assign.setVariable("name", rc.getName());
			assign.setVariable("cls",
					rc.getDomain().getJavaClassName(schemaRootPackageName));
			if (rc.getDomain().isPrimitive()) {
				assign.add("h += ((#cls#) _#name#).hashCode();");
			} else {
				assign.add("h += _#name#.hashCode();");
			}
		}
		code.addNoIndent(new CodeSnippet("\treturn h;", "}"));
		return code;
	}

	private CodeBlock createEqualsMethod() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(true, "@Override",
				"public boolean equals(Object o) {"));
		code.add(new CodeSnippet("if (o == null) {", "\treturn false;", "}"));

		code.add(new CodeSnippet("if (o instanceof #simpleClassName#) {",
				"\t#simpleClassName# rec = (#simpleClassName#) o;"));
		for (RecordComponent rc : recordDomain.getComponents()) {
			CodeSnippet codeSnippet = new CodeSnippet();
			codeSnippet.setVariable("name", rc.getName());
			if (rc.getDomain().isPrimitive()) {
				codeSnippet.add("\tif (_#name# != rec._#name#) {");
				codeSnippet.add("\t\treturn false;", "\t}");
			} else {
				codeSnippet.add("\tif (!(_#name#.equals(rec._#name#))) {");
				codeSnippet.add("\t\treturn false;", "\t\t}");
			}
			code.add(codeSnippet);
		}
		code.add(new CodeSnippet("\treturn true;", "}"));

		code.add(new CodeSnippet("if (o instanceof #jgPackage#.Record) {",
				"\t#jgPackage#.Record rec = (#jgPackage#.Record) o;",
				"\tif (rec.size() != " + recordDomain.getComponents().size()
						+ ") {", "\t\treturn false;", "\t}", "\ttry {"));

		for (RecordComponent rc : recordDomain.getComponents()) {
			CodeSnippet codeSnippet = new CodeSnippet(
					"\t\tif (!rec.getComponent(\"#name#\").equals(_#name#)) {",
					"\t\t\treturn false;", "\t\t}");
			codeSnippet.setVariable("name", rc.getName());
			code.add(codeSnippet);
		}
		code.add(new CodeSnippet("\t\treturn true;",
				"\t} catch (NoSuchAttributeException e) {",
				"\t\treturn false;", "\t}", "}", "return false;"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createHeader() {
		CodeSnippet code = new CodeSnippet(true);
		if (!currentCycle.isClassOnly()) {
			return code;
		}
		addImports("java.util.Collections", "java.util.List",
				"java.util.ArrayList");
		code.add("public class #simpleClassName# implements de.uni_koblenz.jgralab.Record {");
		code.add(
				"\tprivate static List<String> componentNames = new ArrayList<String>("
						+ recordDomain.getComponents().size() + ");", "",
				"\tstatic {");
		for (RecordComponent rc : recordDomain.getComponents()) {
			code.add("\t\tcomponentNames.add(\"" + rc.getName() + "\");");
		}
		code.add(
				"\t\tcomponentNames = Collections.unmodifiableList(componentNames);",
				"\t}");
		code.add("", "\t@Override",
				"\tpublic List<String> getComponentNames() {",
				"\t\treturn componentNames;", "\t}");
		code.add("", "\t@Override",
				"\tpublic boolean hasComponent(String name) {",
				"\t\treturn componentNames.contains(name);", "\t}");
		code.add("", "\t@Override", "public int size() {", "\treturn "
				+ recordDomain.getComponents().size() + ";", "}");
		return code;
	}

	/**
	 * Getter-methods for fields needed for transaction support.
	 *
	 * @return
	 */
	protected CodeBlock createGetterMethods() {
		CodeList code = new CodeList();
		for (RecordComponent rc : recordDomain.getComponents()) {
			CodeSnippet getterCode = new CodeSnippet(true);
			getterCode.setVariable("name", rc.getName());
			getterCode.setVariable("isOrGet", rc.getDomain().isBoolean() ? "is"
					: "get");
			getterCode.setVariable(
					"type",
					rc.getDomain().getJavaAttributeImplementationTypeName(
							schemaRootPackageName));

			getterCode.setVariable(
					"ctype",
					rc.getDomain().getJavaAttributeImplementationTypeName(
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
		addImports("de.uni_koblenz.jgralab.NoSuchAttributeException");
		code.addNoIndent(new CodeSnippet(true, "@Override",
				"public Object getComponent(String name) {"));

		for (RecordComponent rc : recordDomain.getComponents()) {
			CodeBlock assign = null;
			if (currentCycle.isTransImpl()) {
				assign = new CodeSnippet("if (name.equals(\"#name#\")) {",
						"\treturn #isOrGet#_#name#();", "}");
			} else {
				assign = new CodeSnippet("if (name.equals(\"#name#\")) {",
						"\treturn _#name#;", "}");
			}

			assign.setVariable("name", rc.getName());
			assign.setVariable("cname",
					rc.getDomain().getJavaClassName(schemaRootPackageName));
			assign.setVariable("isOrGet", rc.getDomain().isBoolean() ? "is"
					: "get");
			code.add(assign);
		}
		code.add(new CodeSnippet(
				"throw new NoSuchAttributeException(\"#rcname# doesn't contain an attribute \" + name);"));
		code.addNoIndent(new CodeSnippet("}"));
		code.setVariable("rcname", recordDomain.getQualifiedName());
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
		for (RecordComponent rc : recordDomain.getComponents()) {
			code.add(rc.getDomain().getWriteMethod(schemaRootPackageName,
					"_" + rc.getName(), "io"));
		}
		code.addNoIndent(new CodeSnippet("\tio.write(\")\");", "}"));
		return code;
	}

	private CodeBlock createRecordComponents() {
		CodeList code = new CodeList();
		for (RecordComponent rc : recordDomain.getComponents()) {
			Domain dom = rc.getDomain();
			CodeSnippet s = new CodeSnippet(true,
					"private final #type# _#field#;");
			s.setVariable("field", rc.getName());
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
		code.addNoIndent(new CodeSnippet(true, "@Override",
				"public String toString() {",
				"\tStringBuilder sb = new StringBuilder();"));
		String delim = "[";
		for (RecordComponent rc : recordDomain.getComponents()) {
			CodeSnippet s = new CodeSnippet("String #key#String;");
			if (rc.getDomain().isComposite()
					|| (rc.getDomain() instanceof StringDomain)) {
				s.add("if (_#key# == null) #key#String = \"null\";",
						"else #key#String = #toString#;");
			} else {
				s.add("#key#String = #toString#;");
			}
			s.add("sb.append(\"#delim#\").append(\"#key#\").append(\"=\").append(#key#String);");
			s.setVariable("delim", delim);
			s.setVariable("key", rc.getName());
			if (rc.getDomain().isComposite()) {
				s.setVariable("toString", "_" + rc.getName() + ".toString()");
			} else {
				s.setVariable("toString", "String.valueOf(_#key#);");
			}
			code.add(s);
			delim = ", ";
		}
		code.addNoIndent(new CodeSnippet(
				"\treturn sb.append(\"]\").toString();", "}"));
		return code;
	}
}
