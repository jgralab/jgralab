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

// import de.uni_koblenz.jgralab.Graph;
// import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.schema.BooleanDomain; // import
// de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.IntegerDomain; // import
// de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
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
			String schemaPackageName, String implementationName,
			boolean transactionSupport) {
		super(schemaPackageName, recordDomain.getPackageName(),
				transactionSupport);
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
		// getter-methods for components
		code.add(createGetterMethods(createClass));
		// setter-methods for components
		code.add(createSetterMethods(createClass));
		code.add(createFieldConstructor(createClass));
		code.add(createMapConstructor(createClass));
		code.add(createToStringMethod(createClass));
		code.add(createReadComponentsMethod(createClass));
		code.add(createWriteComponentsMethod(createClass));
		// needed for transaction support
		// code.add(createSetVersionedRecordMethod(createClass));
		// clone()-method for record
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
	private CodeBlock createSetNameMethod(boolean createClass) {
		CodeList code = new CodeList();
		if(createClass && transactionSupport) {
			code.addNoIndent(new CodeSnippet(true, "public void setName(String name) {"));
			code.add(new CodeSnippet("this.name = name;"));
			for(Entry<String, Domain> entry : recordDomain.getComponents().entrySet()) {
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
		if (createClass && transactionSupport) {
			code.addNoIndent(new CodeSnippet(true,
					"public boolean equals(Object o) {"));
			// code.add(new CodeSnippet("\tif(this == o)"));
			// code.add(new CodeSnippet("\t\treturn true;"));
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
				// code.add(new CodeSnippet("\t#comptype# this_#name# =
				// null;"));
				// code
				// .add(new CodeSnippet(
				// "\tif(_#name#.hasTemporaryValue(graph.getCurrentTransaction()))"));
				// code
				// .add(new CodeSnippet(
				// "\t\tthis_#name# =
				// _#name#.getTemporaryValue(graph.getCurrentTransaction());"));
				// code.add(new CodeSnippet("\telse"));
				// code
				// .add(new CodeSnippet(
				// "\t\tthis_#name# = _#name#.getLatestPersistentValue();"));
				// code
				// .add(new CodeSnippet(
				// "\tif (!(this_#name# == null &&
				// record._#name#.getLatestPersistentValue() == null) &&
				// this_#name# != null "
				// + "&&
				// !this_#name#.equals(record._#name#.getLatestPersistentValue()))"));
				/*
				 * code .add(new CodeSnippet( "\tif (!(_" + name +
				 * ".getTemporaryValue(graph.getCurrentTransaction()) == null && _" +
				 * name + ".getLatestPersistentValue() == null) && _" + name +
				 * ".getTemporaryValue(graph.getCurrentTransaction()) != null &&
				 * !_" + name +
				 * ".getTemporaryValue(graph.getCurrentTransaction()).equals(_" +
				 * name + ".getLatestPersistentValue()))"));
				 */
				// code.add(new CodeSnippet("\t\treturn false;"));
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
		if (createClass && transactionSupport) {
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
		if (createClass && transactionSupport) {
			code.addNoIndent(new CodeSnippet(true,
					"private void init(Graph g) {"));
			code.add(new CodeSnippet("if (g == null)"));
			code
					.add(new CodeSnippet(
							"\tthrow new GraphException(\"Given graph cannot be null.\");"));
			code.add(new CodeSnippet("if (!g.hasTransactionSupport())"));
			code
					.add(new CodeSnippet(
							"\tthrow new GraphException("
									+ "\"An instance of "
									+ recordDomain
											.getTransactionJavaClassName(schemaRootPackageName)
									+ " can only be created for graphs with transaction support.\");"));
			code.add(new CodeSnippet("\tgraph = g;"));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		// return new CodeSnippet(true, "public class #simpleClassName# {");
		if (transactionSupport)
			addImports("#jgTransPackage#.JGraLabCloneable",
			// "#jgImplTransPackage#.VersionedJGraLabCloneableImpl",
					"#jgPackage#.Graph", "#jgPackage#.GraphException");
		CodeSnippet code = null;
		if (createClass) {
			// TODO fix
			// addImports(schemaRootPackageName + ".#simpleClassName#");
			addImports("#schemaPackage#.#simpleClassName#");
			if (transactionSupport) {
				// addImports("#schemaImplTransPackage#.#simpleClassName#");
				code = new CodeSnippet(true,
						"public class #simpleImplClassName# extends #simpleClassName#"
								+ " implements JGraLabCloneable" + " {");
				// code
				// .add("\tprivate
				// VersionedJGraLabCloneableImpl<#simpleImplClassName#>
				// versionedRecord;");
				code.add("\tprivate Graph graph;");
				code.add("\tprivate String name;");

			} else {
				// addImports("#schemaImplStdPackage#.#simpleClassName#");
				code = new CodeSnippet(true,
						"public class #simpleImplClassName# extends #simpleClassName# {");
			}
		} else {
			// abstract class (or better use interface?)
			code = new CodeSnippet(true,
					"public abstract class #simpleClassName# " + " {");
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
			if (transactionSupport)
				getterCode.setVariable("ctype", rdc.getValue()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
			else
				getterCode.setVariable("ctype", rdc.getValue()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
			if (rdc.getValue().isComposite()) {
				getterCode.add("@SuppressWarnings(\"unchecked\")");
			}
			if (!createClass) {
				// abstract class (or better use interface?)
				getterCode.add("public abstract #type# #isOrGet#_#name#();");
			} else {
				getterCode.add("public #type# #isOrGet#_#name#() {");
				if (transactionSupport) {
					/*
					 * getterCode.add("\tif(versionedRecord == null)");
					 * getterCode .add("\t\tthrow new
					 * GraphException(\"Versioning is not working for this
					 * Record.\");");
					 */
				} else
					getterCode.add("\treturn _#name#;");
				if (transactionSupport) {
					// if (rdc.getValue().isComposite()) {
					// getterCode
					// .add("\treturn (#ctype#)
					// versionedRecord.getValidValue(graph.getCurrentTransaction())._#name#.clone();");
					// } else {
					// getterCode
					// .add("\treturn
					// versionedRecord.getValidValue(graph.getCurrentTransaction())._#name#.getValidValue(graph.getCurrentTransaction());");
					getterCode
							.add("\t#ctype# value = _#name#.getValidValue(graph.getCurrentTransaction());");
					if(rdc.getValue().isComposite())
						getterCode.add("\tvalue.setName(name + \"_#name#\");");
					getterCode.add("\treturn value;");
					// }
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
			if (transactionSupport)
				setterCode.setVariable("ctype", rdc.getValue()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
			else
				setterCode.setVariable("ctype", rdc.getValue()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
			if (!createClass)
				// abstract
				setterCode.add("public abstract void #setter#;");
			else {
				setterCode.add("public void #setter# {");
				if (!transactionSupport) 
					setterCode.add("\tthis._#name# = (#ctype#) _#name#;");
				if (transactionSupport) {
					if (rdc.getValue().isComposite()) {
						setterCode
								.add("\tif(_#name# != null && !(_#name# instanceof "
										+ rdc.getValue()
												.getTransactionJavaClassName(
														schemaRootPackageName)
										+ "))");
						setterCode
								.add("\t\tthrow new GraphException(\"The given parameter of type "
										+ rdc.getValue().getSimpleName()
										+ " doesn't support transactions.\");");
						setterCode
								.add("\tif(_#name# != null && ((#jgTransPackage#.JGraLabCloneable)_#name#).getGraph() != graph)");
						setterCode
								.add("\t\tthrow new GraphException(\"The given parameter of type "
										+ rdc.getValue().getSimpleName()
										+ " belongs to another graph.\");");
					}
					setterCode.add("\tif(graph.isLoading()) {");
					setterCode.add("\t\t this._#name# = new "
							+ rdc.getValue().getVersionedClass(
									schemaRootPackageName)
							+ "(graph, (#ctype#) _#name#);");
					setterCode.add("\t\t this._#name#.setPartOfRecord(true);");
					setterCode.add("\t}");
					setterCode.add("\tif(this._#name# == null) {");
					setterCode.add("\t\t this._#name# = new "
							+ rdc.getValue().getVersionedClass(
									schemaRootPackageName) + "(graph);");
					setterCode.add("\t\t this._#name#.setPartOfRecord(true);");
					setterCode.add("\t}");
					if(rdc.getValue().isComposite()) 
						setterCode.add("\t((JGraLabCloneable)_#name#).setName(name + \"_#name#\");");
					setterCode
							.add("\tthis._#name#.setValidValue((#ctype#) _#name#, graph.getCurrentTransaction());");
				}

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
			if (transactionSupport)
				header = new CodeSnippet(true,
						"public #simpleImplClassName#(Graph g, #fields#) {");
			else
				header = new CodeSnippet(true,
						"public #simpleImplClassName#(#fields#) {");
			code.addNoIndent(header);
			if (transactionSupport)
				code.add(new CodeSnippet("init(g);"));
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
				// if ((rdc.getValue() instanceof CollectionDomainImpl)
				// || (rdc.getValue() instanceof MapDomainImpl)) {
				// String attrImplTypeName = null;
				if (transactionSupport) {
					/*
					 * attrImplTypeName = rdc.getValue()
					 * .getTransactionJavaAttributeImplementationTypeName(
					 * schemaRootPackageName); assign = new
					 * CodeSnippet("this._#name# = new " +
					 * rdc.getValue().getVersionedClass( schemaRootPackageName) +
					 * "((" + attrImplTypeName + ") _#name#);");
					 */
					assign = new CodeSnippet("set_#name#(_#name#);");
				} else {
					/*
					 * attrImplTypeName = rdc.getValue()
					 * .getJavaAttributeImplementationTypeName(
					 * schemaRootPackageName);
					 */
					assign = new CodeSnippet("this._#name# = _#name#;");
				}
				// } else {
				// assign = new CodeSnippet("this._#name# = _#name#;");
				// }
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
			if (transactionSupport)
				code
						.addNoIndent(new CodeSnippet(false,
								"public #simpleImplClassName#(Graph g, java.util.Map<String, Object> fields) {"));
			else
				code
						.addNoIndent(new CodeSnippet(false,
								"public #simpleImplClassName#(java.util.Map<String, Object> fields) {"));
			if (transactionSupport)
				code.add(new CodeSnippet("init(g);"));
			for (Entry<String, Domain> rdc : recordDomain.getComponents()
					.entrySet()) {
				CodeBlock assign = null;
				if (transactionSupport)

					/*
					 * assign = new CodeSnippet("this._#name# = new " +
					 * rdc.getValue().getVersionedClass( schemaRootPackageName) +
					 * "((" + rdc.getValue().getTransactionJavaClassName(
					 * schemaRootPackageName) + ")fields.get(\"#name#\"));");
					 */
					assign = new CodeSnippet("set_#name#(("
							+ rdc.getValue().getJavaClassName(
									schemaRootPackageName)
							+ ")fields.get(\"#name#\"));");
				else
					assign = new CodeSnippet("this._#name# = ("
							+ rdc.getValue().getJavaClassName(
									schemaRootPackageName)
							+ ")fields.get(\"#name#\");");
				assign.setVariable("name", rdc.getKey());
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
			if (transactionSupport)
				code
						.addNoIndent(new CodeSnippet(true,
								"public #simpleImplClassName#(Graph g, GraphIO io) throws GraphIOException {"));
			else
				code
						.addNoIndent(new CodeSnippet(true,
								"public #simpleImplClassName#(GraphIO io) throws GraphIOException {"));
			if (transactionSupport)
				code.add(new CodeSnippet("init(g);"));
			code.add(new CodeSnippet("io.match(\"(\");"));
			for (Entry<String, Domain> c : recordDomain.getComponents()
					.entrySet()) {
				if (transactionSupport)
					code.add(c.getValue().getTransactionReadMethod(
							schemaRootPackageName, "tmp_" + c.getKey(), "io"));
				/*
				 * else
				 * code.add(c.getValue().getReadMethod(schemaRootPackageName,
				 * "tmp_" + c.getKey(), "io"));
				 */
				if (transactionSupport)
					/*
					 * code .add(new CodeSnippet( "_" + c.getKey() + "= new " +
					 * c.getValue().getVersionedClass( schemaRootPackageName) +
					 * "((" + c .getValue()
					 * .getTransactionJavaAttributeImplementationTypeName(
					 * schemaRootPackageName) + ") tmp_" + c.getKey() + ");"));
					 */
					code
							.add(new CodeSnippet(
									"set_"
											+ c.getKey()
											+ "(("
											+ c
													.getValue()
													.getTransactionJavaAttributeImplementationTypeName(
															schemaRootPackageName)
											+ ") tmp_" + c.getKey() + ");"));
				else
					code.add(c.getValue().getReadMethod(schemaRootPackageName,
							"_" + c.getKey(), "io"));
				// code.setVariable("name", c.getKey());
			}
			code.add(new CodeSnippet("io.match(\")\");"));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	private CodeBlock createWriteComponentsMethod(boolean createClass) {
		CodeList code = new CodeList();
		// abstract class (or better use interface?)
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
				// code.setVariable("isOrGet",
				// c.getValue() instanceof BooleanDomain ? "is" : "get");
				String isOrGet = c.getValue() instanceof BooleanDomain ? "is"
						: "get";
				code.add(c.getValue().getWriteMethod(schemaRootPackageName,
						isOrGet + "_" + c.getKey() + "()", "io"));
				// code.add(c.getValue().getWriteMethod(schemaRootPackageName,
				// "#isOrGet#_" + c.getKey() + "()", "io"));
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
				String fieldType = null;
				if (transactionSupport)
					// fieldType = dom
					// .getTransactionJavaAttributeImplementationTypeName(schemaRootPackageName);
					fieldType = dom.getVersionedClass(schemaRootPackageName);
				else
					fieldType = dom
							.getJavaAttributeImplementationTypeName(schemaRootPackageName);
				if (dom.isComposite() || dom instanceof EnumDomain
						|| dom instanceof StringDomain) {
					CodeSnippet s = new CodeSnippet(true,
							"protected #type# _#field#;");
					s.setVariable("field", rdc.getKey());
					s.setVariable("type", fieldType);
					code.addNoIndent(s);
				} else {
					// CodeSnippet s = new CodeSnippet(true,
					// "protected #type# _#field# =
					// #ntype#.valueOf(#initValue#);");
					CodeSnippet s = new CodeSnippet(true,
							"protected #type# _#field#;");
					// TODO check if it works
					// fieldType = dom.getJavaClassName(schemaRootPackageName);
					s.setVariable("field", rdc.getKey());
					s.setVariable("type", fieldType);
					s.setVariable("ntype", dom
							.getJavaClassName(schemaRootPackageName));
					s.setVariable("initValue",
							dom instanceof BooleanDomain ? "false"
									: dom instanceof IntegerDomain ? "0"
											: "0.0");
					code.addNoIndent(s);
				}
			}
		}
		return code;
	}

	/**
	 * Creates the toString()-method for this record domain
	 */
	private CodeBlock createToStringMethod(boolean createClass) {
		CodeList code = new CodeList();
		// abstract class (or better use interface?)
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
		if (transactionSupport) {
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
			StringBuilder constructorFields = new StringBuilder();
			StringBuilder versionedComponents = new StringBuilder();
			int count = 0;
			int size = recordDomain.getComponents().entrySet().size();
			// TODO use construct in separate code!!!
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
					constructorFields.append("_" + rdc.getKey()
							+ ".getValidValue(graph.getCurrentTransaction())\n\t\t");
				}
				if ((count + 1) != size) {
					constructorFields.append(", ");
				}
				// TODO maybe this is not necessary or even leads to unexpected behaviour?
				versionedComponents.append("record._" + rdc.getKey() + " =_"
						+ rdc.getKey() + ";\n\t\t");
				count++;
			}
			code.add(new CodeSnippet(
					"#simpleImplClassName# record = new #simpleImplClassName#(graph, "
							+ constructorFields.toString() + ");"));
			// TODO maybe this is not necessary or even leads to unexpected behaviour?
			code.add(new CodeSnippet(true, versionedComponents.toString()));
			code.add(new CodeSnippet("return record;"));
			/*
			 * code .add(new CodeSnippet("#simpleImplClassName# clone = new
			 * #simpleImplClassName#(" + constructorFields.toString() + ");"));
			 * code.add(new
			 * CodeSnippet("clone.setVersionedRecord(versionedRecord);"));
			 * code.add(new CodeSnippet("return clone;"));
			 */
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	/**
	 * Creates setVersionedRecord()-method.
	 * 
	 * @return
	 */
	/*
	 * private CodeBlock createSetVersionedRecordMethod(boolean createClass) {
	 * CodeList code = new CodeList(); if (transactionSupport) { code
	 * .addNoIndent(new CodeSnippet( true, "protected void
	 * setVersionedRecord(VersionedJGraLabCloneableImpl<#simpleImplClassName#>
	 * versionedRecord) {", "\tthis.versionedRecord = versionedRecord;",
	 * "\tgraph = versionedRecord.getGraph();", "}")); } return code; }
	 */

}
