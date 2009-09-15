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

import java.util.Map.Entry;

import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.impl.CollectionDomainImpl;
import de.uni_koblenz.jgralab.schema.impl.MapDomainImpl;

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
			String schemaPackageName, String implementationName) {
		super(schemaPackageName, recordDomain.getPackageName(), true);
		rootBlock.setVariable("simpleClassName", recordDomain.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", null);
		rootBlock.setVariable("isClassOnly", "true");
		this.recordDomain = recordDomain;
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = new CodeList();
		code.add(createRecordComponents());
		// getter-methods for fields
		code.add(createGetterMethods());
		// setter-methods for fields
		code.add(createSetterMethods());
		code.add(createFieldConstructor());
		code.add(createMapConstructor());
		code.add(createToStringMethod());
		code.add(createReadComponentsMethod());
		code.add(createWriteComponentsMethod());
		// needed for transaction support
		code.add(createSetVersionedRecordMethod());
		// clone()-method for record
		code.add(createCloneMethod());
		return code;
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		// return new CodeSnippet(true, "public class #simpleClassName# {");
		addImports(
				"de.uni_koblenz.jgralab.trans.JGraLabCloneable",
				"de.uni_koblenz.jgralab.impl.trans.VersionedJGraLabCloneableImpl",
				"de.uni_koblenz.jgralab.Graph");
		// every Record needs to implement Interface JGraLabCloneable
		CodeSnippet code = new CodeSnippet(true,
				"public class #simpleClassName# "
						+ "implements JGraLabCloneable" + " {");
		code
				.add("\tprivate VersionedJGraLabCloneableImpl<#simpleClassName#> versionedRecord;");
		code.add("\tprivate Graph graph;");
		return code;
	}

	/**
	 * Getter-methods for fields needed for tranasction support.
	 * 
	 * @return
	 */
	protected CodeBlock createGetterMethods() {
		CodeList code = new CodeList();
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			CodeSnippet getterCode = new CodeSnippet(true);
			getterCode.setVariable("name", rdc.getKey());
			getterCode.setVariable("cName", camelCase(rdc.getKey()));
			getterCode.setVariable("isOrGet", rdc.getValue().getJavaClassName(
					schemaRootPackageName).equals("Boolean") ? "is" : "get");
			getterCode.setVariable("type", rdc.getValue()
					.getJavaAttributeImplementationTypeName(
							schemaRootPackageName));
			getterCode.setVariable("ctype", rdc.getValue()
					.getTransactionJavaAttributeImplementationTypeName(
							schemaRootPackageName));
			if (rdc.getValue().isComposite()) {
				getterCode.add("@SuppressWarnings(\"unchecked\")");
			}
			getterCode.add("public #type# #isOrGet##cName#() {");
			getterCode.add("\tif(versionedRecord == null)");
			getterCode.add("\t\treturn #name#;");
			if (rdc.getValue().isComposite()) {
				getterCode
						.add("\treturn (#ctype#) versionedRecord.getValidValue(graph.getCurrentTransaction()).#name#.clone();");
			} else {
				getterCode
						.add("\treturn versionedRecord.getValidValue(graph.getCurrentTransaction()).#name#;");
			}
			getterCode.add("}");
			code.addNoIndent(getterCode);
		}
		return code;
	}

	/**
	 * Setter-methods for fields needed for tranasction support.
	 * 
	 * @return
	 */
	protected CodeBlock createSetterMethods() {
		CodeList code = new CodeList();
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			CodeSnippet setterCode = new CodeSnippet(true);
			setterCode.setVariable("name", rdc.getKey());
			setterCode.setVariable("setter", "set" + camelCase(rdc.getKey())
					+ "(#type# #name#)");
			setterCode.setVariable("type", rdc.getValue()
					.getJavaAttributeImplementationTypeName(
							schemaRootPackageName));
			setterCode.setVariable("ctype", rdc.getValue()
					.getTransactionJavaAttributeImplementationTypeName(
							schemaRootPackageName));
			setterCode.add("public void #setter# {");
			setterCode.add("\tif(versionedRecord == null)");
			setterCode.add("\t\tthis.#name# = (#ctype#) #name#;");
			setterCode
					.add("\tversionedRecord.setValidValue(this, graph.getCurrentTransaction());");
			setterCode
					.add("\tversionedRecord.getValidValue(graph.getCurrentTransaction()).#name# = (#ctype#) #name#;");
			setterCode.add("}");
			code.addNoIndent(setterCode);
		}
		return code;
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

			CodeBlock assign = null;
			if ((rdc.getValue() instanceof CollectionDomainImpl)
					|| (rdc.getValue() instanceof MapDomainImpl)) {
				String attrImplTypeName = rdc.getValue()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName);
				assign = new CodeSnippet("this.#name# = new "
						+ attrImplTypeName + "(#name#);");
			} else {
				assign = new CodeSnippet("this.#name# = #name#;");
			}
			assign.setVariable("name", rdc.getKey());
			code.add(assign);
		}
		header.setVariable("fields", sb.toString());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	private CodeBlock createMapConstructor() {
		CodeList code = new CodeList();
		// suppress "unchecked" warnings if this record domain contains a
		// Collection domain (Set<E>, List<E>, Map<K, V>)
		for (Domain d : recordDomain.getComponents().values()) {
			if (d.isComposite() && !(d instanceof RecordDomain)) {
				code.addNoIndent(new CodeSnippet(true,
						"@SuppressWarnings(\"unchecked\")"));
				break;
			}
		}
		code
				.addNoIndent(new CodeSnippet(false,
						"public #simpleClassName#(java.util.Map<String, Object> fields) {"));
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			CodeBlock assign = new CodeSnippet("this.#name# = ("
					+ rdc.getValue().getTransactionJavaClassName(
							schemaRootPackageName) + ")fields.get(\"#name#\");");
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
						"public #simpleClassName#(GraphIO _io) throws GraphIOException {"));
		code.add(new CodeSnippet("_io.match(\"(\");"));
		for (Entry<String, Domain> c : recordDomain.getComponents().entrySet()) {
			code.add(c.getValue().getTransactionReadMethod(
					schemaRootPackageName, "_" + c.getKey(), "_io"));
			code.add(new CodeSnippet(c.getKey()
					+ "= ("
					+ c.getValue()
							.getTransactionJavaAttributeImplementationTypeName(
									schemaRootPackageName) + ") _" + c.getKey()
					+ ";"));
		}
		code.add(new CodeSnippet("_io.match(\")\");"));
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
						"public void writeComponentValues(GraphIO _io) throws IOException, GraphIOException {",
						"\t_io.writeSpace();", "\t_io.write(\"(\");",
						"\t_io.noSpace();"));

		for (Entry<String, Domain> c : recordDomain.getComponents().entrySet()) {
			code.add(c.getValue().getWriteMethod(schemaRootPackageName,
					c.getKey(), "_io"));
		}

		code.addNoIndent(new CodeSnippet("\t_io.write(\")\");", "}"));
		return code;
	}

	private CodeBlock createRecordComponents() {
		CodeList code = new CodeList();
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			CodeSnippet s = new CodeSnippet(true, "protected #type# #field#;");
			s.setVariable("type", rdc.getValue()
					.getTransactionJavaAttributeImplementationTypeName(
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
					"sb.append(\"#key#\");", "sb.append(\"=\");",
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

	/**
	 * Creates the clone()-method for the record.
	 * 
	 * @return
	 */
	private CodeBlock createCloneMethod() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(true,
				"@SuppressWarnings(\"unchecked\")", "public Object clone() {"));
		String constructorFields = "";
		int count = 0;
		int size = recordDomain.getComponents().entrySet().size();
		// TODO use construct in own code!!!
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			if (rdc.getValue().isComposite()) {
				constructorFields += "("
						+ rdc
								.getValue()
								.getTransactionJavaAttributeImplementationTypeName(
										schemaRootPackageName) + ") "
						+ rdc.getKey() + ".clone()";
			} else {
				constructorFields += rdc.getKey();
			}
			if ((count + 1) != size) {
				constructorFields += ", ";
			}
			count++;
		}
		code.addNoIndent(new CodeSnippet("\treturn new #simpleClassName#("
				+ constructorFields + ");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	/**
	 * Creates setVersionedRecord()-method.
	 * 
	 * @return
	 */
	private CodeBlock createSetVersionedRecordMethod() {
		CodeList code = new CodeList();
		code
				.addNoIndent(new CodeSnippet(
						true,
						"public void setVersionedRecord(VersionedJGraLabCloneableImpl<#simpleClassName#> versionedRecord) {",
						"\tthis.versionedRecord = versionedRecord;",
						"\tgraph = versionedRecord.getGraph();", "}"));

		return code;
	}

}
