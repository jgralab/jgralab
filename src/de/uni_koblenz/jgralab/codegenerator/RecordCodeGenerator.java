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
import de.uni_koblenz.jgralab.schema.MapDomain;
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
		rootBlock.setVariable("theGraph", "g");
		this.recordDomain = recordDomain;
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = new CodeList();
		code.add(createRecordComponents());
		code.add(createGetterMethods());
		code.add(createSetterMethods());
		code.add(createVariableParametersConstructor());
		code.add(createFieldConstructor());
		code.add(createMapConstructor());
		code.add(createToStringMethod());
		code.add(createReadComponentsMethod());
		code.add(createWriteComponentsMethod());
		code.add(createCloneMethod());
		code.add(createInitMethod());
		code.add(createGetGraphMethod());
		code.add(createEqualsMethod());
		code.add(createSetNameMethod());
		return code;
	}

	/**
	 * 
	 * @param createClass
	 * @return
	 */
	private CodeBlock createVariableParametersConstructor() {
		CodeList code = new CodeList();

		if (currentCycle.isStdOrTransImpl()) {
			CodeSnippet codeSnippet = new CodeSnippet(true);

			if (hasCompositeRecordComponent()) {
				codeSnippet.add("@SuppressWarnings(\"unchecked\")");
			}

			codeSnippet
					.add("protected #simpleImplClassName#(Graph g, Object... components) {");

			if (currentCycle.isTransImpl()) {
				codeSnippet.add("\tinit(g);");
			} else if (hasCompositeRecordComponent()) {
				codeSnippet.add("\tgraph = g;");
			}

			code.addNoIndent(codeSnippet);

			int count = 0;
			for (Entry<String, Domain> rdc : recordDomain.getComponents()
					.entrySet()) {

				CodeSnippet assign = null;

				if (currentCycle.isTransImpl()) {
					assign = new CodeSnippet(
							"\tset_#name#((#type#) components[#index#]);");
				} else {
					assign = new CodeSnippet(
							"\tthis._#name# = (#type#) components[#index#];");
				}

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
	private CodeBlock createSetNameMethod() {
		CodeList code = new CodeList();
		if (currentCycle.isTransImpl()) {
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
	private CodeBlock createEqualsMethod() {
		CodeList code = new CodeList();
		if (currentCycle.isTransImpl()) {
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

	private CodeBlock createGetGraphMethod() {
		CodeList code = new CodeList();
		if (currentCycle.isTransImpl()) {
			code
					.addNoIndent(new CodeSnippet(true,
							"public Graph getGraph() {"));
			code.add(new CodeSnippet("return graph;"));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createInitMethod() {
		CodeList code = new CodeList();
		if (currentCycle.isTransImpl()) {
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
	protected CodeBlock createHeader() {
		CodeSnippet code = null;
		switch (currentCycle) {
		case ABSTRACT:
			code = new CodeSnippet(true,
					"public abstract class #simpleClassName# {");
			break;
		case STDIMPL:
			addImports("#jgPackage#.Graph");
			addImports("#schemaPackage#.#simpleClassName#");
			code = new CodeSnippet(true,
					"public class #simpleImplClassName# extends #simpleClassName# {");
			if (hasCompositeRecordComponent()) {
				code.add("\tprivate Graph graph;");
			}
			break;
		case TRANSIMPL:
			addImports("#jgPackage#.Graph");
			addImports("#schemaPackage#.#simpleClassName#");
			addImports("#jgTransPackage#.JGraLabCloneable",
					"#jgPackage#.GraphException");
			code = new CodeSnippet(true,
					"public class #simpleImplClassName# extends #simpleClassName#"
							+ " implements JGraLabCloneable {");
			code.add("\tprivate String name;");
			code.add("\tprivate Graph graph;");
			break;
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
		for (Entry<String, Domain> rdc : recordDomain.getComponents()
				.entrySet()) {
			CodeSnippet getterCode = new CodeSnippet(true);
			getterCode.setVariable("name", rdc.getKey());
			getterCode.setVariable("isOrGet", rdc.getValue().getJavaClassName(
					schemaRootPackageName).equals("Boolean") ? "is" : "get");
			getterCode.setVariable("type", rdc.getValue()
					.getJavaAttributeImplementationTypeName(
							schemaRootPackageName));

			switch (currentCycle) {
			case ABSTRACT:
				getterCode.add("public abstract #type# #isOrGet#_#name#();");
				break;
			case STDIMPL:
				getterCode.setVariable("ctype", rdc.getValue()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				getterCode.add("public #type# #isOrGet#_#name#() {");
				getterCode.add("\treturn _#name#;");
				getterCode.add("}");
				break;
			case TRANSIMPL:
				getterCode.setVariable("ctype", rdc.getValue()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				getterCode.add("public #type# #isOrGet#_#name#() {");
				getterCode
						.add("\t#ctype# value = _#name#.getValidValue(graph.getCurrentTransaction());");
				if (rdc.getValue().isComposite()) {
					getterCode.add("\tif(_#name# != null)");
					getterCode.add("\t\tvalue.setName(name + \"_#name#\");");
				}
				getterCode.add("\treturn value;");
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
	protected CodeBlock createSetterMethods() {
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

			switch (currentCycle) {
			case ABSTRACT:
				setterCode.add("public abstract void #setter#;");
				break;
			case STDIMPL:
				setterCode.setVariable("ctype", rdc.getValue()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				setterCode.add("public void #setter# {");
				setterCode.add("\tthis._#name# = (#ctype#) _#name#;");
				setterCode.add("}");
				break;
			case TRANSIMPL:
				setterCode.setVariable("ctype", rdc.getValue()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				setterCode.setVariable("dvclass", rdc.getValue()
						.getVersionedClass(schemaRootPackageName));

				setterCode.add("public void #setter# {");
				if (rdc.getValue().isComposite()) {
					String genericType = "";
					if (!(rdc.getValue() instanceof RecordDomain)) {
						genericType = "<?>";
					}
					if (rdc.getValue() instanceof MapDomain) {
						genericType = "<?,?>";
					}

					setterCode.setVariable("dname", rdc.getValue()
							.getSimpleName());
					setterCode
							.setVariable("dtransclass", rdc.getValue()
									.getTransactionJavaClassName(
											schemaRootPackageName));

					setterCode
							.add("\tif(_#name# != null && !(_#name# instanceof #dtransclass#"
									+ genericType + "))");
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
				setterCode.add("}");
				break;
			}
			code.addNoIndent(setterCode);
		}
		return code;
	}

	private CodeBlock createFieldConstructor() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrTransImpl()) {
			StringBuilder sb = new StringBuilder();
			CodeSnippet header = null;
			header = new CodeSnippet(true,
					"protected #simpleImplClassName#(Graph g, #fields#) {");

			code.addNoIndent(header);
			if (currentCycle.isTransImpl()) {
				code.add(new CodeSnippet("init(g);"));
			} else if (hasCompositeRecordComponent()) {
				code.add(new CodeSnippet("graph = g;"));
			}

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

				if (currentCycle.isTransImpl()) {
					assign = new CodeSnippet("set_#name#(_#name#);");
				} else {
					assign = new CodeSnippet("this._#name# = _#name#;");
				}

				assign.setVariable("name", rdc.getKey());
				code.add(assign);
			}
			header.setVariable("fields", sb.toString());
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createMapConstructor() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrTransImpl()) {
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

			if (currentCycle.isTransImpl()) {
				code.add(new CodeSnippet("init(g);"));
			} else if (hasCompositeRecordComponent()) {
				code.add(new CodeSnippet("graph=g;"));
			}

			for (Entry<String, Domain> rdc : recordDomain.getComponents()
					.entrySet()) {
				CodeBlock assign = null;
				if (currentCycle.isTransImpl()) {
					assign = new CodeSnippet(
							"set_#name#((#cname#)fields.get(\"#name#\"));");
				} else {
					assign = new CodeSnippet(
							"this._#name# = (#cname#)fields.get(\"#name#\");");
				}

				assign.setVariable("name", rdc.getKey());
				assign.setVariable("cname", rdc.getValue().getJavaClassName(
						schemaRootPackageName));
				code.add(assign);
			}
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createReadComponentsMethod() {
		CodeList code = new CodeList();
		// abstract class (or better use interface?)
		if (currentCycle.isStdOrTransImpl()) {
			addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
			code
					.addNoIndent(new CodeSnippet(
							true,
							"protected #simpleImplClassName#(Graph g, GraphIO io) throws GraphIOException {"));

			if (currentCycle.isTransImpl()) {
				code.add(new CodeSnippet("init(g);"));
			} else if (hasCompositeRecordComponent()) {
				code.add(new CodeSnippet("graph = g;"));
			}

			code.add(new CodeSnippet("io.match(\"(\");"));
			for (Entry<String, Domain> c : recordDomain.getComponents()
					.entrySet()) {
				if (currentCycle.isTransImpl()) {
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

	private CodeBlock createWriteComponentsMethod() {
		CodeList code = new CodeList();
		if (currentCycle.isAbstract()) {
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

	private CodeBlock createRecordComponents() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrTransImpl()) {
			for (Entry<String, Domain> rdc : recordDomain.getComponents()
					.entrySet()) {
				Domain dom = rdc.getValue();

				CodeSnippet s = new CodeSnippet(true,
						"private #type# _#field#;");
				s.setVariable("field", rdc.getKey());

				if (currentCycle.isTransImpl()) {
					s.setVariable("type", dom
							.getVersionedClass(schemaRootPackageName));
				} else {
					s
							.setVariable(
									"type",
									dom
											.getJavaAttributeImplementationTypeName(schemaRootPackageName));
				}
				code.addNoIndent(s);
			}
		}
		return code;
	}

	/**
	 * Creates the toString()-method for this record domain
	 */
	private CodeBlock createToStringMethod() {
		CodeList code = new CodeList();

		if (currentCycle.isAbstract()) {
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
	private CodeBlock createCloneMethod() {
		CodeList code = new CodeList();
		if (currentCycle.isTransImpl()) {

			boolean suppressWarningsNeeded = false;
			for (Domain dom : recordDomain.getComponents().values()) {
				if (dom.isComposite() && !(dom instanceof RecordDomain)) {
					suppressWarningsNeeded = true;
					break;
				}
			}

			if (suppressWarningsNeeded) {
				code.addNoIndent(new CodeSnippet(true,
						"@SuppressWarnings(\"unchecked\")"));
			}

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
	private boolean hasCompositeRecordComponent() {
		for (Entry<String, Domain> entry : recordDomain.getComponents()
				.entrySet()) {
			if (entry.getValue().isComposite()) {
				return true;
			}
		}
		return false;
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
