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
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.StringDomain;
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
			String schemaPackageName, String implementationName,
			boolean transactionSupport) {
		super(schemaPackageName, recordDomain.getPackageName(),
				transactionSupport);
		rootBlock.setVariable("simpleClassName", recordDomain.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", recordDomain
				.getSimpleName()
				+ "Impl");
		// one abstract class and two implementation classes need to be generated
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
		code.add(createSetVersionedRecordMethod(createClass));
		// clone()-method for record
		code.add(createCloneMethod(createClass));
		return code;
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		// return new CodeSnippet(true, "public class #simpleClassName# {");
		if (transactionSupport)
			addImports(
					"#jgTransPackage#.JGraLabCloneable",
					"#jgImplTransPackage#.VersionedJGraLabCloneableImpl",
					"#jgPackage#.Graph");
		CodeSnippet code = null;
		if (createClass) {
			addImports(schemaRootPackageName + ".#simpleClassName#");
			if (transactionSupport) {
				code = new CodeSnippet(true,
						"public class #simpleImplClassName# extends #simpleClassName#"
								+ " implements JGraLabCloneable" + " {");
				code
						.add("\tprivate VersionedJGraLabCloneableImpl<#simpleImplClassName#> versionedRecord;");
				code.add("\tprivate Graph graph;");
			} else
				code = new CodeSnippet(true,
						"public class #simpleImplClassName# extends #simpleClassName# {");
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
				if (transactionSupport)
					// TODO think about if this is still necessary
					getterCode.add("\tif(versionedRecord == null)");
				getterCode.add("\t\treturn _#name#;");
				if (transactionSupport) {
					if (rdc.getValue().isComposite()) {
						getterCode
								.add("\treturn (#ctype#) versionedRecord.getValidValue(graph.getCurrentTransaction())._#name#.clone();");
					} else {
						getterCode
								.add("\treturn versionedRecord.getValidValue(graph.getCurrentTransaction())._#name#;");
					}
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
				if (transactionSupport)
					// TODO think about this is still necessary?!
					setterCode.add("\tif(versionedRecord == null)");
				setterCode.add("\t\tthis._#name# = (#ctype#) _#name#;");
				if (transactionSupport) {
					setterCode
							.add("\tversionedRecord.setValidValue(this, graph.getCurrentTransaction());");
					setterCode
							.add("\tversionedRecord.getValidValue(graph.getCurrentTransaction())._#name# = (#ctype#) _#name#;");
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
			CodeSnippet header = new CodeSnippet(true,
					"public #simpleImplClassName#(#fields#) {");
			code.addNoIndent(header);
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
				if ((rdc.getValue() instanceof CollectionDomainImpl)
						|| (rdc.getValue() instanceof MapDomainImpl)) {
					String attrImplTypeName = null;
					if (transactionSupport) {
						attrImplTypeName = rdc
								.getValue()
								.getTransactionJavaAttributeImplementationTypeName(
										schemaRootPackageName);
						assign = new CodeSnippet("this._#name# = new "
								+ attrImplTypeName + "(_#name#);");
					} else {
						/*
						 * attrImplTypeName = rdc.getValue()
						 * .getJavaAttributeImplementationTypeName(
						 * schemaRootPackageName);
						 */
						assign = new CodeSnippet("this._#name# = _#name#;");
					}
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
					.addNoIndent(new CodeSnippet(false,
							"public #simpleImplClassName#(java.util.Map<String, Object> fields) {"));
			for (Entry<String, Domain> rdc : recordDomain.getComponents()
					.entrySet()) {
				CodeBlock assign = null;
				if (transactionSupport)
					assign = new CodeSnippet("this._#name# = ("
							+ rdc.getValue().getTransactionJavaClassName(
									schemaRootPackageName)
							+ ")fields.get(\"#name#\");");
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
			code
					.addNoIndent(new CodeSnippet(true,
							"public #simpleImplClassName#(GraphIO io) throws GraphIOException {"));
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
					code
							.add(new CodeSnippet(
									"_"
											+ c.getKey()
											+ "= ("
											+ c
													.getValue()
													.getTransactionJavaAttributeImplementationTypeName(
															schemaRootPackageName)
											+ ") tmp_" + c.getKey() + ";"));
				else
					code.add(c.getValue().getReadMethod(schemaRootPackageName,
							"_" + c.getKey(), "io"));
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
				//code.setVariable("isOrGet",
				//	c.getValue() instanceof BooleanDomain ? "is" : "get");
				String isOrGet = c.getValue() instanceof BooleanDomain ? "is" : "get";
				code.add(c.getValue().getWriteMethod(schemaRootPackageName,
						isOrGet + "_" + c.getKey() + "()", "io"));
				//code.add(c.getValue().getWriteMethod(schemaRootPackageName,
					//	"#isOrGet#_" + c.getKey() + "()", "io"));
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
					fieldType = dom
							.getTransactionJavaAttributeImplementationTypeName(schemaRootPackageName);
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
					CodeSnippet s = new CodeSnippet(true,
							"protected #type# _#field# = #type#.valueOf(#initValue#);");
					// TODO check if it works
					fieldType = dom.getJavaClassName(schemaRootPackageName);
					s.setVariable("field", rdc.getKey());
					s.setVariable("type", fieldType);
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
									+ ") _" + rdc.getKey() + ".clone()");
				} else {
					constructorFields.append("_" + rdc.getKey());
				}
				if ((count + 1) != size) {
					constructorFields.append(", ");
				}
				count++;
			}
			code.add(new CodeSnippet("return new #simpleImplClassName#("
					+ constructorFields.toString() + ");"));
			code.addNoIndent(new CodeSnippet("}"));
		}
		return code;
	}

	/**
	 * Creates setVersionedRecord()-method.
	 * 
	 * @return
	 */
	private CodeBlock createSetVersionedRecordMethod(boolean createClass) {
		CodeList code = new CodeList();
		if (transactionSupport) {
			code
					.addNoIndent(new CodeSnippet(
							true,
							// TODO check if it needs to be public?
							"protected void setVersionedRecord(VersionedJGraLabCloneableImpl<#simpleImplClassName#> versionedRecord) {",
							"\tthis.versionedRecord = versionedRecord;",
							"\tgraph = versionedRecord.getGraph();", "}"));
		}
		return code;
	}

}
