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

import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.RecordDomain;

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
			String schemaPackageName, String implementationName,
			CodeGeneratorConfiguration config) {
		super(schemaPackageName, recordDomain.getPackageName(), config);
		rootBlock.setVariable("simpleClassName", recordDomain.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", recordDomain
				.getSimpleName()
				+ "Impl");
		// one abstract class and two implementation classes need to be
		// generated
		rootBlock.setVariable("isClassOnly", "false");
		this.recordDomain = recordDomain;
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = new CodeList();
		code.add(createRecordComponents(createClass));
		code.add(createGetterMethods(createClass));
		code.add(createSetterMethods(createClass));
		code.add(createVariableParametersConstructor(createClass));
		code.add(createFieldConstructor(createClass));
		code.add(createMapConstructor(createClass));
		code.add(createToStringMethod(createClass));
		code.add(createReadComponentsMethod(createClass));
		code.add(createWriteComponentsMethod(createClass));
		code.add(createCloneMethod(createClass));
		code.add(createInitMethod(createClass));
		code.add(createGetGraphMethod(createClass));
		code.add(createEqualsMethod(createClass));
		code.add(createSetNameMethod(createClass));
		return code;
	}

	/**
	 * 
	 * @param createClass
	 * @return
	 */
	private CodeBlock createVariableParametersConstructor(boolean createClass) {
		CodeList code = new CodeList();

		if (createClass) {
			CodeSnippet codeSnippet = new CodeSnippet(true,
					"@SuppressWarnings(\"unchecked\")");

			codeSnippet
					.add("protected #simpleImplClassName#(Graph g, Object... components) {");

			if (config.hasTransactionSupport())
				codeSnippet.add("\tinit(g);");
			else
				codeSnippet.add("\tgraph = g;");

			code.addNoIndent(codeSnippet);

			int count = 0;
			for (Entry<String, Domain> rdc : recordDomain.getComponents()
					.entrySet()) {

				CodeSnippet assign = null;

				if (config.hasTransactionSupport())
					assign = new CodeSnippet(
							"\tset_#name#((#type#) components[#index#]);");
				else
					assign = new CodeSnippet(
							"\tthis._#name# = (#type#) components[#index#];");

				assign.setVariable("name", rdc.getKey());
				assign.setVariable("type", rdc.getValue().getJavaClassName(
						schemaRootPackageName));

				assign.setVariable("index", new Integer(count).toString());

				code.addNoIndent(assign);
				count++;
			}
			code.addNoIndent(new CodeSnippet("}"));
		}

		return code;
	}

	/**
	 * 
	 * @param createClass
	 * @return
	 */
	private CodeBlock createSetNameMethod(boolean createClass) {
		CodeList code = new CodeList();
		if (createClass && config.hasTransactionSupport()) {
			code.addNoIndent(new CodeSnippet(true,
					"public void setName(String name) {"));
			code.add(new CodeSnippet("this.name = name;"));

			for (Entry<String, Domain> entry : recordDomain.getComponents()
					.entrySet()) {
				CodeSnippet codeSnippet = new CodeSnippet();
				codeSnippet.add("if(_#name# != null)");
				codeSnippet.add("\t_#name#.setName(this.name + \"_#name#\");");
				codeSnippet.setVariable("name", entry.getKey());
				code.add(codeSnippet);
			}

			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	/**
	 * 
	 * @param createClass
	 * @return
	 */
	private CodeBlock createEqualsMethod(boolean createClass) {
		CodeList code = new CodeList();
		if (createClass && config.hasTransactionSupport()) {
			code.addNoIndent(new CodeSnippet(true,
					"public boolean equals(Object o) {"));
			code
					.add(new CodeSnippet(
							"#simpleImplClassName# record = (#simpleImplClassName#) o;"));
			for (Entry<String, Domain> entry : recordDomain.getComponents()
					.entrySet()) {
				CodeSnippet codeSnippet = new CodeSnippet(true);
				codeSnippet.add("\t#comptype# this_#name# = null;");
				codeSnippet
						.add("\tif(_#name#.hasTemporaryValue(graph.getCurrentTransaction()))");
				codeSnippet
						.add("\t\tthis_#name# = _#name#.getTemporaryValue(graph.getCurrentTransaction());");
				codeSnippet.add("\telse");
				codeSnippet
						.add("\t\tthis_#name# = _#name#.getLatestPersistentValue();");
				codeSnippet
						.add("\t#comptype# that_#name# = record._#name#.getLatestPersistentValue();");
				codeSnippet
						.add("\tif (!(this_#name# == null && that_#name# == null) && this_#name# != null "
								+ "&& !this_#name#.equals(that_#name#))");
				codeSnippet.add("\t\treturn false;");
				code.addNoIndent(codeSnippet);
				codeSnippet.setVariable("comptype", entry.getValue()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				codeSnippet.setVariable("name", entry.getKey());
			}
			code.add(new CodeSnippet("\treturn true;"));
			code.add(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createGetGraphMethod(boolean createClass) {
		CodeList code = new CodeList();
		if (createClass && config.hasTransactionSupport()) {
			code
					.addNoIndent(new CodeSnippet(true,
							"public Graph getGraph() {"));
			code.add(new CodeSnippet("return graph;"));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createInitMethod(boolean createClass) {
		CodeList code = new CodeList();
		if (createClass && config.hasTransactionSupport()) {
			CodeSnippet codeSnippet = new CodeSnippet(true,
					"private void init(Graph g) {");

			codeSnippet.add("if (g == null)");
			codeSnippet
					.add("\tthrow new GraphException(\"Given graph cannot be null.\");");
			codeSnippet.add("if (!g.hasTransactionSupport())");
			codeSnippet
					.add("\tthrow new GraphException("
							+ "\"An instance of #tclassname# can only be created for graphs with transaction support.\");");
			codeSnippet.add("\tgraph = g;");
			codeSnippet.add("\tgraph = g;");
			codeSnippet.add("}");

			codeSnippet.setVariable("tclassname", recordDomain
					.getTransactionJavaClassName(schemaRootPackageName));

			code.addNoIndent(codeSnippet);
		}
		return code;
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		if (config.hasTransactionSupport() && createClass)
			addImports("#jgTransPackage#.JGraLabCloneable",
					"#jgPackage#.GraphException");
		CodeSnippet code = null;
		if (createClass) {
			addImports("#jgPackage#.Graph");
			addImports("#schemaPackage#.#simpleClassName#");
			if (config.hasTransactionSupport()) {
				code = new CodeSnippet(true,
						"public class #simpleImplClassName# extends #simpleClassName#"
								+ " implements JGraLabCloneable {");
				code.add("\tprivate String name;");
				code.add("\tprivate Graph graph;");
			} else {
				code = new CodeSnippet(true,
						"public class #simpleImplClassName# extends #simpleClassName# {");
				code.add("\t@SuppressWarnings(\"unused\")");
				code.add("\tprivate Graph graph;");
			}
		} else {
			code = new CodeSnippet(true,
					"public abstract class #simpleClassName# {");
		}
		return code;
	}

	/**
	 * Getter-methods for fields needed for transaction support.
	 * 
	 * @return
	 */
	protected CodeBlock createGetterMethods(boolean createClass) {
		CodeList code = new CodeList();
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			CodeSnippet getterCode = new CodeSnippet(true);
			getterCode.setVariable("name", rdc.getKey());
			getterCode.setVariable("isOrGet", rdc.getValue().getJavaClassName(
					schemaRootPackageName).equals("Boolean") ? "is" : "get");
			getterCode.setVariable("type", rdc.getValue()
					.getJavaAttributeImplementationTypeName(
							schemaRootPackageName));

			if (config.hasTransactionSupport())
				getterCode.setVariable("ctype", rdc.getValue()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
			else
				getterCode.setVariable("ctype", rdc.getValue()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
			if (!createClass) {
				getterCode.add("public abstract #type# #isOrGet#_#name#();");
			} else {
				getterCode.add("public #type# #isOrGet#_#name#() {");

				if (config.hasTransactionSupport()) {
					getterCode
							.add("\t#ctype# value = _#name#.getValidValue(graph.getCurrentTransaction());");
					if (rdc.getValue().isComposite()) {
						getterCode.add("\tif(_#name# != null)");
						getterCode
								.add("\t\tvalue.setName(name + \"_#name#\");");
					}
					getterCode.add("\treturn value;");
				} else {
					getterCode.add("\treturn _#name#;");
				}

				getterCode.add("}");
			}
			code.addNoIndent(getterCode);
		}
		return code;
	}

	/**
	 * Setter-methods for fields needed for transaction support.
	 * 
	 * @return
	 */
	protected CodeBlock createSetterMethods(boolean createClass) {
		CodeList code = new CodeList();
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			CodeSnippet setterCode = new CodeSnippet(true);
			setterCode.setVariable("name", rdc.getKey());
			setterCode.setVariable("setter", "set_" + rdc.getKey()
					+ "(#type# _#name#)");
			setterCode.setVariable("type", rdc.getValue()
					.getJavaAttributeImplementationTypeName(
							schemaRootPackageName));

			if (config.hasTransactionSupport())
				setterCode.setVariable("ctype", rdc.getValue()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
			else
				setterCode.setVariable("ctype", rdc.getValue()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));

			if (!createClass)
				setterCode.add("public abstract void #setter#;");
			else {
				setterCode.add("public void #setter# {");

				if (config.hasTransactionSupport()) {
					setterCode.setVariable("dvclass", rdc.getValue()
							.getVersionedClass(schemaRootPackageName));

					if (rdc.getValue().isComposite()) {
						setterCode.setVariable("dname", rdc.getValue()
								.getSimpleName());
						setterCode.setVariable("dtransclass", rdc.getValue()
								.getTransactionJavaClassName(
										schemaRootPackageName));

						setterCode
								.add("\tif(_#name# != null && !(_#name# instanceof #dtransclass#))");
						setterCode
								.add("\t\tthrow new GraphException(\"The given parameter of type #dname# doesn't support transactions.\");");
						setterCode
								.add("\tif(_#name# != null && ((#jgTransPackage#.JGraLabCloneable)_#name#).getGraph() != graph)");
						setterCode
								.add("\t\tthrow new GraphException(\"The given parameter of type #dname# belongs to another graph.\");");
					}

					setterCode.add("\tif(graph.isLoading()) {");
					setterCode
							.add("\t\t this._#name# = new #dvclass#(graph, (#ctype#) _#name#);");
					setterCode.add("\t\t this._#name#.setPartOfRecord(true);");
					setterCode.add("\t}");
					setterCode.add("\tif(this._#name# == null) {");
					setterCode.add("\t\t this._#name# = new #dvclass#(graph);");
					setterCode.add("\t\t this._#name#.setPartOfRecord(true);");
					setterCode.add("\t}");

					if (rdc.getValue().isComposite()) {
						setterCode.add("\tif(_#name# != null)");
						setterCode
								.add("\t\t((JGraLabCloneable)_#name#).setName(name + \"_#name#\");");
					}

					setterCode
							.add("\tthis._#name#.setValidValue((#ctype#) _#name#, graph.getCurrentTransaction());");
				} else
					setterCode.add("\tthis._#name# = (#ctype#) _#name#;");

				setterCode.add("}");
			}
			code.addNoIndent(setterCode);
		}
		return code;
	}

	private CodeBlock createFieldConstructor(boolean createClass) {
		CodeList code = new CodeList();
		if (createClass) {
			StringBuilder sb = new StringBuilder();
			CodeSnippet header = null;
			header = new CodeSnippet(true,
					"protected #simpleImplClassName#(Graph g, #fields#) {");

			code.addNoIndent(header);
			if (config.hasTransactionSupport())
				code.add(new CodeSnippet("init(g);"));
			else
				code.add(new CodeSnippet("graph = g;"));

			String delim = "";
			for (Entry<String, Domain> rdc : recordDomain.getComponents()
					.entrySet()) {
				sb.append(delim);
				delim = ", ";
				sb.append(rdc.getValue()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				sb.append(" _");
				sb.append(rdc.getKey());

				CodeBlock assign = null;

				if (config.hasTransactionSupport())
					assign = new CodeSnippet("set_#name#(_#name#);");
				else
					assign = new CodeSnippet("this._#name# = _#name#;");

				assign.setVariable("name", rdc.getKey());
				code.add(assign);
			}
			header.setVariable("fields", sb.toString());
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createMapConstructor(boolean createClass) {
		CodeList code = new CodeList();
		if (createClass) {
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
					.addNoIndent(new CodeSnippet(
							false,
							"protected #simpleImplClassName#(Graph g, java.util.Map<String, Object> fields) {"));

			if (config.hasTransactionSupport())
				code.add(new CodeSnippet("init(g);"));
			else
				code.add(new CodeSnippet("graph=g;"));

			for (Entry<String, Domain> rdc : recordDomain.getComponents()
					.entrySet()) {
				CodeBlock assign = null;
				if (config.hasTransactionSupport())
					assign = new CodeSnippet(
							"set_#name#((#cname#)fields.get(\"#name#\"));");
				else
					assign = new CodeSnippet(
							"this._#name# = (#cname#)fields.get(\"#name#\");");

				assign.setVariable("name", rdc.getKey());
				assign.setVariable("cname", rdc.getValue().getJavaClassName(
						schemaRootPackageName));
				code.add(assign);
			}
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createReadComponentsMethod(boolean createClass) {
		CodeList code = new CodeList();
		// abstract class (or better use interface?)
		if (createClass) {
			addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
			code
					.addNoIndent(new CodeSnippet(
							true,
							"protected #simpleImplClassName#(Graph g, GraphIO io) throws GraphIOException {"));

			if (config.hasTransactionSupport())
				code.add(new CodeSnippet("init(g);"));
			else
				code.add(new CodeSnippet("graph = g;"));

			code.add(new CodeSnippet("io.match(\"(\");"));
			for (Entry<String, Domain> c : recordDomain.getComponents()
					.entrySet()) {
				if (config.hasTransactionSupport()) {
					code.add(c.getValue().getTransactionReadMethod(
							schemaRootPackageName, "tmp_" + c.getKey(), "io"));
					CodeSnippet cs = new CodeSnippet(
							"set_#key#((#typeName#) tmp_#key#);");

					cs.setVariable("key", c.getKey());
					cs.setVariable("typeName", c.getValue()
							.getTransactionJavaAttributeImplementationTypeName(
									schemaRootPackageName));
					code.add(cs);
				} else {
					code.add(c.getValue().getReadMethod(schemaRootPackageName,
							"_" + c.getKey(), "io"));
				}
			}
			code.add(new CodeSnippet("io.match(\")\");"));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createWriteComponentsMethod(boolean createClass) {
		CodeList code = new CodeList();
		if (!createClass) {
			addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException",
					"java.io.IOException");
			code
					.addNoIndent(new CodeSnippet(
							true,
							"public void writeComponentValues(GraphIO io) throws IOException, GraphIOException {",
							"\tio.writeSpace();", "\tio.write(\"(\");",
							"\tio.noSpace();"));

			for (Entry<String, Domain> c : recordDomain.getComponents()
					.entrySet()) {
				String isOrGet = c.getValue() instanceof BooleanDomain ? "is"
						: "get";
				code.add(c.getValue().getWriteMethod(schemaRootPackageName,
						isOrGet + "_" + c.getKey() + "()", "io"));
			}

			code.addNoIndent(new CodeSnippet("\tio.write(\")\");", "}"));
		}
		return code;
	}

	private CodeBlock createRecordComponents(boolean createClass) {
		CodeList code = new CodeList();
		if (createClass) {
			for (Entry<String, Domain> rdc : recordDomain.getComponents()
					.entrySet()) {
				Domain dom = rdc.getValue();

				CodeSnippet s = new CodeSnippet(true,
						"private #type# _#field#;");
				s.setVariable("field", rdc.getKey());

				if (config.hasTransactionSupport())
					s.setVariable("type", dom
							.getVersionedClass(schemaRootPackageName));
				else
					s
							.setVariable(
									"type",
									dom
											.getJavaAttributeImplementationTypeName(schemaRootPackageName));
				code.addNoIndent(s);
			}
		}
		return code;
	}

	/**
	 * Creates the toString()-method for this record domain
	 */
	private CodeBlock createToStringMethod(boolean createClass) {
		CodeList code = new CodeList();

		if (!createClass) {
			code.addNoIndent(new CodeSnippet(true,
					"public String toString() {",
					"\tStringBuilder sb = new StringBuilder();"));
			String delim = "[";
			for (Entry<String, Domain> c : recordDomain.getComponents()
					.entrySet()) {
				CodeSnippet s = new CodeSnippet("sb.append(\"#delim#\");",
						"sb.append(\"#key#\");", "sb.append(\"=\");",
						"sb.append(#isOrGet#_#key#()#toString#);");
				Domain domain = c.getValue();
				s
						.setVariable("isOrGet", domain.getJavaClassName(
								schemaRootPackageName).equals("Boolean") ? "is"
								: "get");
				s.setVariable("delim", delim);
				s.setVariable("key", c.getKey());
				s.setVariable("toString", domain.isComposite() ? ".toString()"
						: "");
				code.add(s);
				delim = ", ";
			}
			code.addNoIndent(new CodeSnippet("\tsb.append(\"]\");",
					"\treturn sb.toString();", "}"));
		}

		return code;
	}

	/**
	 * Creates the clone()-method for the record.
	 * 
	 * @return
	 */
	private CodeBlock createCloneMethod(boolean createClass) {
		CodeList code = new CodeList();
		if (config.hasTransactionSupport() && createClass) {

			boolean suppressWarningsNeeded = false;
			for (Domain dom : recordDomain.getComponents().values()) {
				if (dom.isComposite() && !(dom instanceof RecordDomain)) {
					suppressWarningsNeeded = true;
					break;
				}
			}

			if (suppressWarningsNeeded)
				code.addNoIndent(new CodeSnippet(true,
						"@SuppressWarnings(\"unchecked\")"));

			code.addNoIndent(new CodeSnippet("public Object clone() {"));

			code.add(new CodeSnippet(
					"#simpleImplClassName# record = new #simpleImplClassName#(graph, "
							+ getConstructorParametersOutput() + ");"));

			// TODO maybe this is not necessary or even leads to unexpected
			// behaviour?
			code.add(new CodeSnippet(true, getVersionedComponentsOutput()));
			code.add(new CodeSnippet("return record;"));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	/**
	 * 
	 * @return
	 */
	private String getVersionedComponentsOutput() {
		StringBuilder versionedComponents = new StringBuilder();
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			versionedComponents.append("record._" + rdc.getKey() + " =_"
					+ rdc.getKey() + ";\n\t\t");
		}
		return versionedComponents.toString();
	}

	/**
	 * 
	 * @return
	 */
	private String getConstructorParametersOutput() {
		StringBuilder constructorFields = new StringBuilder();
		int count = 0;
		int size = recordDomain.getComponents().entrySet().size();
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			if (rdc.getValue().isComposite()) {
				constructorFields
						.append("("
								+ rdc
										.getValue()
										.getTransactionJavaAttributeImplementationTypeName(
												schemaRootPackageName)
								+ ") _"
								+ rdc.getKey()
								+ ".getValidValue(graph.getCurrentTransaction()).clone()\n\t\t");
			} else {
				constructorFields
						.append("_"
								+ rdc.getKey()
								+ ".getValidValue(graph.getCurrentTransaction())\n\t\t");
			}
			if ((count + 1) != size) {
				constructorFields.append(", ");
			}
			count++;
		}
		return constructorFields.toString();
	}

}
