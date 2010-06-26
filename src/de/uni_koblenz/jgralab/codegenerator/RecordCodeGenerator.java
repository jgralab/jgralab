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
			String schemaPackageName, String implementationName,
			CodeGeneratorConfiguration config) {
		super(schemaPackageName, recordDomain.getPackageName(), config);
		rootBlock.setVariable("simpleClassName", recordDomain.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", recordDomain
				.getSimpleName()
				+ "Impl");
		rootBlock.setVariable("theGraph", "graph");
		this.recordDomain = recordDomain;
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = new CodeList();
		code.add(createRecordComponents());
		code.add(createGetterMethods());
		code.add(createSetterMethods());
		code.add(createVariableParametersConstructor());
		code.add(createDefaultConstructor());
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
	 * Default constructor needed for transaction support.
	 * 
	 * @return
	 */
	private CodeBlock createDefaultConstructor() {
		CodeList code = new CodeList();
		if (currentCycle.isTransImpl()) {
			CodeSnippet header = null;
			header = new CodeSnippet(true,
					"protected #simpleImplClassName#(Graph g) {");
			code.addNoIndent(header);
			code.add(new CodeSnippet("#theGraph# = g;"));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	/**
	 * 
	 * @param createClass
	 * @return
	 */
	private CodeBlock createVariableParametersConstructor() {
		CodeList code = new CodeList();

		if (currentCycle.isStdOrSaveMemOrTransImpl()) {
			CodeSnippet codeSnippet = new CodeSnippet(true);

			if (hasCompositeRecordComponent()) {
				codeSnippet.add("@SuppressWarnings(\"unchecked\")");
			}

			codeSnippet
					.add("protected #simpleImplClassName#(Graph g, Object... components) {");

			if (currentCycle.isTransImpl()) {
				codeSnippet.add("\tinit(g);");
			} else /* if (hasCompositeRecordComponent()) */{
				codeSnippet.add("\tgraph = g;");
			}

			code.addNoIndent(codeSnippet);

			int count = 0;
			for (RecordComponent rdc : recordDomain.getComponents()) {

				CodeSnippet assign = null;

				if (currentCycle.isTransImpl()) {
					assign = new CodeSnippet(
							"\tset_#name#((#type#) components[#index#]);");
				} else {
					assign = new CodeSnippet(
							"\tthis._#name# = (#type#) components[#index#];");
				}

				assign.setVariable("name", rdc.getName());
				assign.setVariable("type", rdc.getDomain().getJavaClassName(
						schemaRootPackageName));

				assign.setVariable("index", Integer.valueOf(count).toString());

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

			for (RecordComponent rdc : recordDomain.getComponents()) {
				CodeSnippet codeSnippet = new CodeSnippet();
				codeSnippet.add("if(_#name# != null)");
				codeSnippet.add("\t_#name#.setName(this.name + \"_#name#\");");
				codeSnippet.setVariable("name", rdc.getName());
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
		code.addNoIndent(new CodeSnippet(true,
				"public boolean equals(Object o) {"));
		code.add(new CodeSnippet("if(o == null)", "\treturn false;"));
		if (currentCycle.isTransImpl()) {
			addImports("#jgTransPackage#.TransactionState");
			code.add(new CodeSnippet("if(!(o instanceof #simpleClassName#))",
					"\treturn false;"));
		}
		if (currentCycle.isStdImpl() || currentCycle.isSaveMemImpl()) {
			code.add(new CodeSnippet(
					"if(!(o instanceof #simpleImplClassName#))",
					"\treturn false;"));
			code
					.add(new CodeSnippet(
							"#simpleImplClassName# record = (#simpleImplClassName#) o;"));
		}

		CodeSnippet codeSnippet = null;
		for (RecordComponent entry : recordDomain.getComponents()) {
			switch (currentCycle) {
			case TRANSIMPL:
				codeSnippet = new CodeSnippet(true);
				codeSnippet.add("\t#comptype# this_#name# = null;");
				codeSnippet
						.add("\tthis_#name# = _#name#.getValidValue(#theGraph#.getCurrentTransaction());");
				codeSnippet.add("\t#comptype# that_#name# = null;");
				codeSnippet
						.add("\tif(o instanceof #jgTransPackage#.JGraLabTransactionCloneable) {");
				codeSnippet
						.add("\t\t#simpleImplClassName# record = (#simpleImplClassName#) o;");
				codeSnippet
						.add("\t\tif(#theGraph#.getCurrentTransaction().getState().equals(TransactionState.VALIDATING))");
				codeSnippet
						.add("\t\t\tthat_#name# = record._#name#.getLatestPersistentValue();");
				codeSnippet.add("\t\telse");
				codeSnippet
						.add("\t\t\tthat_#name# = record._#name#.getValidValue(#theGraph#.getCurrentTransaction());");
				codeSnippet.add("\t} else {");
				codeSnippet
						.add("\t\t#simpleClassName# record = (#simpleClassName#) o;");
				codeSnippet.add("\t\tthat_#name# = record.#isOrGet#_#name#();");
				codeSnippet.add("\t}");
				codeSnippet
						.add("\tif (!(this_#name# == null && that_#name# == null) && this_#name# != null "
								+ "&& !this_#name#.equals(that_#name#))");
				codeSnippet.add("\t\treturn false;");
				code.addNoIndent(codeSnippet);
				if (!entry.getDomain().isComposite()) {
					codeSnippet.setVariable("comptype", entry.getDomain()
							.getTransactionJavaAttributeImplementationTypeName(
									schemaRootPackageName));
				} else {
					codeSnippet.setVariable("comptype", entry.getDomain()
							.getJavaAttributeImplementationTypeName(
									schemaRootPackageName));
				}
				if (entry.getDomain() instanceof BooleanDomain) {
					codeSnippet.setVariable("isOrGet", "is");
				} else {
					codeSnippet.setVariable("isOrGet", "get");
				}
				codeSnippet.setVariable("name", entry.getName());
				break;
			case STDIMPL:
			case SAVEMEMIMPL:
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
				break;
			}
		}
		code.add(new CodeSnippet("\n\t\treturn true;"));
		code.addNoIndent(new CodeSnippet("}\n"));
		return code;
	}

	private CodeBlock createGetGraphMethod() {
		CodeList code = new CodeList();
		if (currentCycle.isTransImpl()) {
			code
					.addNoIndent(new CodeSnippet(true,
							"public Graph getGraph() {"));
			code.add(new CodeSnippet("return #theGraph#;"));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createInitMethod() {
		CodeList code = new CodeList();
		if (currentCycle.isTransImpl()) {
			CodeSnippet codeSnippet = new CodeSnippet(true,
					"private void init(Graph g) {");

			codeSnippet.add("\tif (g == null)");
			codeSnippet
					.add("\t\tthrow new GraphException(\"Given graph cannot be null.\");");
			codeSnippet.add("\tif (!g.hasTransactionSupport())");
			codeSnippet
					.add("\t\tthrow new GraphException("
							+ "\"An instance of #tclassname# can only be created for graphs with transaction support.\");");
			codeSnippet.add("\t\tgraph = g;");
			codeSnippet.add("}");

			codeSnippet
					.setVariable(
							"tclassname",
							recordDomain
									.getTransactionJavaAttributeImplementationTypeName(schemaRootPackageName));

			code.addNoIndent(codeSnippet);
		}
		return code;
	}

	@Override
	protected CodeBlock createHeader() {
		CodeSnippet code = null;
		switch (currentCycle) {
		case ABSTRACT:
			code = new CodeSnippet(
					true,
					"public abstract class #simpleClassName# implements de.uni_koblenz.jgralab.JGraLabCloneable {");
			break;
		case STDIMPL:
		case SAVEMEMIMPL:
			addImports("#jgPackage#.Graph");
			addImports("#schemaPackage#.#simpleClassName#");
			code = new CodeSnippet(true,
					"public class #simpleImplClassName# extends #simpleClassName# {");
			// if (hasCompositeRecordComponent()) {
			code.add("\tprivate Graph #theGraph#;");
			// }
			break;
		case TRANSIMPL:
			addImports("#jgPackage#.Graph");
			addImports("#schemaPackage#.#simpleClassName#");
			addImports("#jgTransPackage#.JGraLabTransactionCloneable",
					"#jgPackage#.GraphException");
			code = new CodeSnippet(true,
					"public class #simpleImplClassName# extends #simpleClassName#"
							+ " implements JGraLabTransactionCloneable {");
			code.add("\tprivate String name;");
			code.add("\tprivate Graph #theGraph#;");
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
		for (RecordComponent rdc : recordDomain.getComponents()) {
			CodeSnippet getterCode = new CodeSnippet(true);
			getterCode.setVariable("name", rdc.getName());
			getterCode.setVariable("isOrGet", rdc.getDomain().getJavaClassName(
					schemaRootPackageName).equals("Boolean") ? "is" : "get");
			getterCode.setVariable("type", rdc.getDomain()
					.getJavaAttributeImplementationTypeName(
							schemaRootPackageName));

			switch (currentCycle) {
			case ABSTRACT:
				getterCode.add("public abstract #type# #isOrGet#_#name#();");
				break;
			case STDIMPL:
			case SAVEMEMIMPL:
				getterCode.setVariable("ctype", rdc.getDomain()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				getterCode.add("public #type# #isOrGet#_#name#() {");
				getterCode.add("\treturn _#name#;");
				getterCode.add("}");
				break;
			case TRANSIMPL:
				getterCode.setVariable("ctype", rdc.getDomain()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				getterCode.add("public #type# #isOrGet#_#name#() {");
				getterCode
						.add("\t#ctype# value = _#name#.getValidValue(#theGraph#.getCurrentTransaction());");
				if (rdc.getDomain().isComposite()) {
					getterCode.add("\tvalue.setName(name + \"_#name#\");");
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
		for (RecordComponent rdc : recordDomain.getComponents()) {
			CodeSnippet setterCode = new CodeSnippet(true);
			setterCode.setVariable("name", rdc.getName());
			setterCode.setVariable("setter", "set_" + rdc.getName()
					+ "(#type# _#name#)");
			setterCode.setVariable("type", rdc.getDomain()
					.getJavaAttributeImplementationTypeName(
							schemaRootPackageName));

			switch (currentCycle) {
			case ABSTRACT:
				setterCode.add("public abstract void #setter#;");
				break;
			case STDIMPL:
			case SAVEMEMIMPL:
				setterCode.setVariable("ctype", rdc.getDomain()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				setterCode.add("public void #setter# {");
				setterCode.add("\tthis._#name# = (#ctype#) _#name#;");
				setterCode.add("}");
				break;
			case TRANSIMPL:
				setterCode.setVariable("ctype", rdc.getDomain()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				setterCode.setVariable("dvclass", rdc.getDomain()
						.getVersionedClass(schemaRootPackageName));

				setterCode.add("public void #setter# {");
				if (rdc.getDomain().isComposite()) {
					setterCode.setVariable("dname", rdc.getDomain()
							.getSimpleName());

					setterCode
							.add("\tif(_#name# != null && !(_#name# instanceof #jgTransPackage#.JGraLabTransactionCloneable))");
					setterCode
							.add("\t\tthrow new GraphException(\"The given parameter of type #dname# doesn't support transactions.\");");
					setterCode
							.add("\tif(_#name# != null && ((#jgTransPackage#.JGraLabTransactionCloneable)_#name#).getGraph() != #theGraph#)");
					setterCode
							.add("\t\tthrow new GraphException(\"The given parameter of type #dname# belongs to another graph.\");");
				}

				setterCode.add("\tif(#theGraph#.isLoading()) {");
				setterCode
						.add("\t\t this._#name# = new #dvclass#(#theGraph#, (#ctype#) _#name#);");
				setterCode.add("\t\t this._#name#.setPartOfRecord(true);");
				setterCode.add("\t}");
				setterCode.add("\tif(this._#name# == null) {");
				setterCode
						.add("\t\t this._#name# = new #dvclass#(#theGraph#);");
				setterCode.add("\t\t this._#name#.setPartOfRecord(true);");
				setterCode.add("\t}");

				if (rdc.getDomain().isComposite()) {
					setterCode.add("\tif(_#name# != null)");
					setterCode
							.add("\t\t((JGraLabTransactionCloneable)_#name#).setName(name + \"_#name#\");");
				}

				setterCode
						.add("\tthis._#name#.setValidValue((#ctype#) _#name#, #theGraph#.getCurrentTransaction());");
				setterCode.add("}");
				break;
			}
			code.addNoIndent(setterCode);
		}
		return code;
	}

	private CodeBlock createFieldConstructor() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrSaveMemOrTransImpl()) {
			StringBuilder sb = new StringBuilder();
			CodeSnippet header = null;
			header = new CodeSnippet(true,
					"protected #simpleImplClassName#(Graph g, #fields#) {");

			code.addNoIndent(header);
			if (currentCycle.isTransImpl()) {
				code.add(new CodeSnippet("init(g);"));
			} else /* if (hasCompositeRecordComponent()) */{
				code.add(new CodeSnippet("#theGraph# = g;"));
			}

			String delim = "";
			for (RecordComponent rdc : recordDomain.getComponents()) {
				sb.append(delim);
				delim = ", ";
				sb.append(rdc.getDomain()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				sb.append(" _");
				sb.append(rdc.getName());

				CodeBlock assign = null;

				if (currentCycle.isTransImpl()) {
					assign = new CodeSnippet("set_#name#(_#name#);");
				} else {
					assign = new CodeSnippet("this._#name# = _#name#;");
				}

				assign.setVariable("name", rdc.getName());
				code.add(assign);
			}
			header.setVariable("fields", sb.toString());
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createMapConstructor() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrSaveMemOrTransImpl()) {
			// suppress "unchecked" warnings if this record domain contains a
			// Collection domain (Set<E>, List<E>, Map<K, V>)
			for (RecordComponent comp : recordDomain.getComponents()) {
				Domain d = comp.getDomain();
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
			} else /* if (hasCompositeRecordComponent()) */{
				code.add(new CodeSnippet("#theGraph#=g;"));
			}

			for (RecordComponent rdc : recordDomain.getComponents()) {
				CodeBlock assign = null;
				if (currentCycle.isTransImpl()) {
					assign = new CodeSnippet(
							"set_#name#((#cname#)fields.get(\"#name#\"));");
				} else {
					assign = new CodeSnippet(
							"this._#name# = (#cname#)fields.get(\"#name#\");");
				}

				assign.setVariable("name", rdc.getName());
				assign.setVariable("cname", rdc.getDomain().getJavaClassName(
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
		if (currentCycle.isStdOrSaveMemOrTransImpl()) {
			addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
			code
					.addNoIndent(new CodeSnippet(
							true,
							"protected #simpleImplClassName#(Graph g, GraphIO io) throws GraphIOException {"));

			if (currentCycle.isTransImpl()) {
				code.add(new CodeSnippet("init(g);"));
			} else /* if (hasCompositeRecordComponent()) */{
				code.add(new CodeSnippet("#theGraph# = g;"));
			}

			code.add(new CodeSnippet("io.match(\"(\");"));
			for (RecordComponent c : recordDomain.getComponents()) {
				if (currentCycle.isTransImpl()) {
					code.add(c.getDomain().getTransactionReadMethod(
							schemaRootPackageName, "tmp_" + c.getName(), "io"));
					CodeSnippet cs = new CodeSnippet(
							"set_#key#((#typeName#) tmp_#key#);");

					cs.setVariable("key", c.getName());
					cs.setVariable("typeName", c.getDomain()
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

			for (RecordComponent c : recordDomain.getComponents()) {
				String isOrGet = c.getDomain() instanceof BooleanDomain ? "is"
						: "get";
				code.add(c.getDomain().getWriteMethod(schemaRootPackageName,
						isOrGet + "_" + c.getName() + "()", "io"));
			}

			code.addNoIndent(new CodeSnippet("\tio.write(\")\");", "}"));
		}
		return code;
	}

	private CodeBlock createRecordComponents() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrSaveMemOrTransImpl()) {
			for (RecordComponent rdc : recordDomain.getComponents()) {
				Domain dom = rdc.getDomain();

				CodeSnippet s = new CodeSnippet(true,
						"private #type# _#field#;");
				s.setVariable("field", rdc.getName());

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
			for (RecordComponent c : recordDomain.getComponents()) {
				CodeSnippet s = new CodeSnippet("sb.append(\"#delim#\");",
						"sb.append(\"#key#\");", "sb.append(\"=\");",
						"sb.append(#isOrGet#_#key#()#toString#);");
				Domain domain = c.getDomain();
				s
						.setVariable("isOrGet", domain.getJavaClassName(
								schemaRootPackageName).equals("Boolean") ? "is"
								: "get");
				s.setVariable("delim", delim);
				s.setVariable("key", c.getName());
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
		if (currentCycle.isAbstract()) {
			code.addNoIndent(new CodeSnippet(true, "@Override"));

			code
					.addNoIndent(new CodeSnippet(
							"public abstract Object clone();"));
		} else {

			boolean suppressWarningsNeeded = false;
			for (RecordComponent comp : recordDomain.getComponents()) {
				Domain dom = comp.getDomain();
				if (dom.isComposite() && currentCycle.isTransImpl()) {
					suppressWarningsNeeded = true;
					break;
				}
			}

			if (suppressWarningsNeeded) {
				code.addNoIndent(new CodeSnippet(true,
						"@SuppressWarnings(\"unchecked\")"));
			}

			code.addNoIndent(new CodeSnippet(suppressWarningsNeeded ? false
					: true, "@Override"));

			code.addNoIndent(new CodeSnippet("public Object clone() {"));
			if (currentCycle.isStdImpl() || currentCycle.isSaveMemImpl()) {
				StringBuffer arguments = new StringBuffer("#theGraph#");
				for (RecordComponent rdc : recordDomain.getComponents()) {
					boolean hasToBeCloned = rdc.getDomain().isComposite()
							|| rdc.getDomain() instanceof RecordDomain;
					arguments.append(", ");
					if (hasToBeCloned) {
						arguments
								.append("_"
										+ rdc.getName()
										+ "==null?null:((de.uni_koblenz.jgralab.JGraLabCloneable)_"
										+ rdc.getName() + ").clone()");
					} else {
						arguments.append("_" + rdc.getName());
					}
				}
				code.add(new CodeSnippet("return new #simpleImplClassName#("
						+ arguments + ");"));
			} else {

				code
						.add(new CodeSnippet(
								"#simpleImplClassName# record = new #simpleImplClassName#(#theGraph#);"));
				code.add(new CodeSnippet(true,
						getSetVersionedComponentsOutput()));
				// TODO this might be useless
				code.add(getSetClonedComponentsOutput());
				code.add(new CodeSnippet("return record;"));
			}
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	/**
	 * 
	 * @return
	 */
	private String getSetVersionedComponentsOutput() {
		StringBuilder versionedComponents = new StringBuilder();
		for (RecordComponent rdc : recordDomain.getComponents()) {
			versionedComponents.append("record._" + rdc.getName() + " = _"
					+ rdc.getName() + ";\n\t\t");
		}
		return versionedComponents.toString();
	}

	/**
	 * 
	 * @return
	 */
	private boolean hasCompositeRecordComponent() {
		for (RecordComponent comp : recordDomain.getComponents()) {
			if (comp.getDomain().isComposite()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	private CodeSnippet getSetClonedComponentsOutput() {
		CodeSnippet code = new CodeSnippet();
		for (RecordComponent rdc : recordDomain.getComponents()) {
			if (rdc.getDomain().isComposite()) {
				code
						.add("record.set_"
								+ rdc.getName()
								+ "(("
								+ rdc
										.getDomain()
										.getTransactionJavaAttributeImplementationTypeName(
												schemaRootPackageName)
								+ ")_"
								+ rdc.getName()
								+ ".getValidValue(#theGraph#.getCurrentTransaction()).clone());");
			} else {
				code
						.add("record.set_"
								+ rdc.getName()
								+ "(_"
								+ rdc.getName()
								+ ".getValidValue(#theGraph#.getCurrentTransaction()));");
			}
		}
		return code;
	}

}
