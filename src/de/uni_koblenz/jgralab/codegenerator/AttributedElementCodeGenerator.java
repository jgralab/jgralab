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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.impl.ListDomainImpl;
import de.uni_koblenz.jgralab.schema.impl.MapDomainImpl;
import de.uni_koblenz.jgralab.schema.impl.RecordDomainImpl;
import de.uni_koblenz.jgralab.schema.impl.SetDomainImpl;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class AttributedElementCodeGenerator extends CodeGenerator {

	/**
	 * all the interfaces of the class which are being implemented
	 */
	protected SortedSet<String> interfaces;

	/**
	 * the AttributedElementClass to generate code for
	 */
	protected AttributedElementClass aec;

	/**
	 * specifies if the generated code is a special JGraLab class of layer M2
	 * this effects the way the constructor and some methods are built valid
	 * values: "Graph", "Vertex", "Edge", "Incidence"
	 */
	protected AttributedElementCodeGenerator(
			AttributedElementClass attributedElementClass,
			String schemaRootPackageName, String implementationName, 
			boolean transactionSupport) {
		super(schemaRootPackageName, attributedElementClass.getPackageName(),
				transactionSupport);
		aec = attributedElementClass;
		rootBlock.setVariable("ecName", aec.getSimpleName());
		rootBlock.setVariable("qualifiedClassName", aec.getQualifiedName());
		rootBlock.setVariable("schemaName", aec.getSchema().getName());
		rootBlock.setVariable("schemaVariableName", aec.getVariableName());
		rootBlock.setVariable("javaClassName", schemaRootPackageName + "."
				+ aec.getQualifiedName());
		if (!transactionSupport) {
			rootBlock.setVariable("qualifiedImplClassName",
					schemaRootPackageName + ".impl.std"	+ aec.getQualifiedName() + "Impl");
		} else {
			// the implementation class for transaction support
			rootBlock.setVariable("qualifiedImplClassName",
					schemaRootPackageName + ".impl.trans" + aec.getQualifiedName() + "Impl");
		}
		rootBlock.setVariable("simpleClassName", aec.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", aec.getSimpleName() + "Impl");
		rootBlock.setVariable("uniqueClassName", aec.getUniqueName());
		rootBlock.setVariable("schemaPackageName", schemaRootPackageName);

		interfaces = new TreeSet<String>();
		interfaces.add(aec.getQualifiedName());
		rootBlock.setVariable("isAbstractClass", aec.isAbstract() ? "true" : "false"); 
		for (AttributedElementClass superClass : attributedElementClass
				.getDirectSuperClasses()) {
			interfaces.add(superClass.getQualifiedName());
		}
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = new CodeList();
		if (createClass) {
			code.add(createFields(aec.getAttributeList()));
			code.add(createConstructor());
			code.add(createGetAttributedElementClassMethod());
			code.add(createGetM1ClassMethod());
			code.add(createGenericGetter(aec.getAttributeList()));
			code.add(createGenericSetter(aec.getAttributeList()));
			code.add(createGettersAndSetters(aec.getAttributeList(),
					createClass));
			code.add(createReadAttributesMethod(aec.getAttributeList()));
			code.add(createReadAttributesFromStringMethod(aec.getAttributeList()));
			code.add(createWriteAttributesMethod(aec.getAttributeList()));
			code.add(createWriteAttributeToStringMethod(aec.getAttributeList()));
		} else {
			code.add(createGettersAndSetters(aec.getOwnAttributeList(),
					createClass));
		}
		return code;
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("classOrInterface", createClass ? " class"	: " interface");
		code.setVariable("abstract",
				createClass && aec.isAbstract() ? " abstract" : "");
		code.setVariable("impl", createClass && !aec.isAbstract() ? "Impl" : "");
		code.add("public#abstract##classOrInterface# #simpleClassName##impl##extends##implements# {");
		code.setVariable("extends", createClass ? " extends #baseClassName#" : "");

		StringBuffer buf = new StringBuffer();
		if (interfaces.size() > 0) {
			String delim = createClass ? " implements " : " extends ";
			for (String interfaceName : interfaces) {
				if (createClass
						|| !interfaceName.equals(aec.getQualifiedName())) {
					if (interfaceName.equals("Vertex")
							|| interfaceName.equals("Edge")
							|| interfaceName.equals("Aggregation")
							|| interfaceName.equals("Composition")
							|| interfaceName.equals("Graph")) {
						buf.append(delim);
						buf.append("#jgPackage#." + interfaceName);
						delim = ", ";
					} else {
						buf.append(delim);
						buf.append(schemaRootPackageName + "." + interfaceName);
						delim = ", ";
					}
				}
			}
		}
		code.setVariable("implements", buf.toString());
		return code;
	}

	protected CodeBlock createStaticImplementationClassField() {
		return new CodeSnippet(
				true,
				"/**",
				" * refers to the default implementation class of this interface",
				" */",
				"public static final java.lang.Class<#qualifiedImplClassName#> IMPLEMENTATION_CLASS = #qualifiedImplClassName#.class;");
	}

	protected CodeBlock createSpecialConstructorCode() {
		return null;
	}

	protected CodeBlock createConstructor() {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(true,
				"public #simpleClassName#Impl(int id, #jgPackage#.Graph g) {",
				"\tsuper(id, g);"));
		code.add(createSpecialConstructorCode());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	protected CodeBlock createGetAttributedElementClassMethod() {
		return new CodeSnippet(
				true,
				"public final #jgSchemaPackage#.AttributedElementClass getAttributedElementClass() {",
				"\treturn #schemaPackageName#.#schemaName#.instance().#schemaVariableName#;",
				"}");
	}

	protected CodeBlock createGetM1ClassMethod() {
		return new CodeSnippet(
				true,
				"public final java.lang.Class<? extends #jgPackage#.AttributedElement> getM1Class() {",
				"\treturn #javaClassName#.class;", "}");
	}

	protected CodeBlock createGenericGetter(Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		code.addNoIndent(new CodeSnippet(true,
						"public Object getAttribute(String attributeName) throws NoSuchFieldException {"));
		for (Attribute attr : attrSet) {
			CodeSnippet s = new CodeSnippet();
			s.setVariable("name", attr.getName());
			s.setVariable("isOrGet", attr.getDomain().getJavaClassName(schemaRootPackageName).equals("Boolean") ? "is" : "get");
			s.setVariable("cName", camelCase(attr.getName()));
			s.add("if (attributeName.equals(\"#name#\")) return #isOrGet##cName#();");
			code.add(s);
		}
		code.add(new CodeSnippet(
						"throw new NoSuchFieldException(\"#qualifiedClassName# doesn't contain an attribute \" + attributeName);"));
		code.addNoIndent(new CodeSnippet("}"));

		return code;
	}

	protected CodeBlock createGenericSetter(Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		CodeSnippet snip = new CodeSnippet(true);
		boolean suppressWarningsNeeded = false;
		for (Attribute attr : attrSet) {
			if (attr.getDomain().isComposite()) {
				suppressWarningsNeeded = true;
				break;
			}
		}
		if (suppressWarningsNeeded) {
			snip.add("@SuppressWarnings(\"unchecked\")");
		}
		snip.add("public void setAttribute(String attributeName, Object data) throws NoSuchFieldException {");
		code.addNoIndent(snip);
		for (Attribute attr : attrSet) {
			CodeSnippet s = new CodeSnippet();
			s.setVariable("name", attr.getName());
			s.setVariable("cName", camelCase(attr.getName()));

			if (attr.getDomain().isComposite()) {
				s.setVariable("attributeClassName", attr.getDomain()
						.getJavaAttributeImplementationTypeName(
								schemaRootPackageName));
			} else {
				s.setVariable("attributeClassName", attr.getDomain()
						.getJavaClassName(schemaRootPackageName));
			}
			boolean isEnumDomain = false;
			if (attr.getDomain() instanceof EnumDomain) {
				isEnumDomain = true;
			}

			if (isEnumDomain) {
				s.add("if (attributeName.equals(\"#name#\")) {");
				s.add("\tif (data instanceof String) {");
				s
						.add("\t\tset#cName#(#attributeClassName#.valueOf((String) data));");
				s.add("\t} else {");
				s.add("\t\tset#cName#((#attributeClassName#) data);");
				s.add("\t}");
				s.add("\treturn;");
				s.add("}");
			} else {
				s.add("if (attributeName.equals(\"#name#\")) {");
				s.add("\tset#cName#((#attributeClassName#) data);");
				s.add("\treturn;");
				s.add("}");
			}

			code.add(s);
		}
		code.add(new CodeSnippet(
					"throw new NoSuchFieldException(\"#qualifiedClassName# doesn't contain an attribute \" + attributeName);"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	protected CodeBlock createFields(Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		for (Attribute attr : attrSet) {
			code.addNoIndent(createField(attr));
		}
		return code;
	}

	protected CodeBlock createGettersAndSetters(Set<Attribute> attrSet,
			boolean createClass) {
		CodeList code = new CodeList();
		for (Attribute attr : attrSet) {
			code.addNoIndent(createGetter(attr, createClass));
		}
		for (Attribute attr : attrSet) {
			code.addNoIndent(createSetter(attr, createClass));
		}
		return code;
	}

	protected CodeBlock createGetter(Attribute attr, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", attr.getName());
		code.setVariable("cName", camelCase(attr.getName()));
		code.setVariable("type", attr.getDomain()
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));
		code.setVariable("isOrGet", attr.getDomain().getJavaClassName(
				schemaRootPackageName).equals("Boolean") ? "is" : "get");
		if (createClass) {
			if (!transactionSupport)
				code.add("public #type# #isOrGet##cName#() {", 
						    "\treturn #name#;", "}");
			else {
				// getter for transaction support
				code.setVariable("initValue", attr.getDomain().getInitialValue());
				code.setVariable("ttype", attr.getDomain().getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				setGraphReferenceVariable(code);

				code.add("public #type# #isOrGet##cName#() {");

				addCheckValidityCode(code);
				code.add("\tif (#name# == null)",
						 "\t\treturn #initValue#;",
						 "\t#ttype# value = #name#.getValidValue(#graphreference#getCurrentTransaction());",
						 "\tif(value == null)",
						 "\t\treturn #initValue#;", "\treturn value;",
						 "}");
			}
		} else {
			code.add("public #type# #isOrGet##cName#();");
		}
		return code;
	}

	protected void addCheckValidityCode(CodeSnippet code) {
		code.add("\tif (!isValid())",
				 "\t\tthrow new #jgPackage#.GraphException(\"Cannot access attribute '#name#', because \" + this + \" isn't valid in current transaction.\");");
	}

	protected void setGraphReferenceVariable(CodeSnippet code) {
		code.setVariable("graphreference", "graph.");
	}

	protected CodeBlock createSetter(Attribute attr, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", attr.getName());
		code.setVariable("tmpname", attr.getName());
		code.setVariable("cName", camelCase(attr.getName()));
		code.setVariable("type", attr.getDomain()
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));

		if (createClass) {
			if (!transactionSupport)
				code.add("public void set#cName#(#type# #name#) {",
						"\tthis.#name# = #name#;", "\tgraphModified();", "}");
			else {
				// setter for transaction support
				code.setVariable("ttype", attr.getDomain()
						.getTransactionJavaAttributeImplementationTypeName(
								schemaRootPackageName));
				code.setVariable("initLoading", "new "
						+ attr.getDomain().getVersionedClass(
								schemaRootPackageName)
						// TODO check: changed from myGraph to this!!!
						+ "(this, #name#, \"#name#\");");
				code.setVariable("init", "new "
						+ attr.getDomain().getVersionedClass(
						// TODO check: changed from myGraph to this!!!
								schemaRootPackageName) + "(this);");
				code.add("public void set#cName#(#type# #name#) {");
				addCheckValidityCode(code);

				setGraphReferenceVariable(code);

				Domain domain = attr.getDomain();
				if (domain.isComposite()) {
					if (!(domain instanceof RecordDomainImpl)) {
						code.setVariable("tmpname", "tmp" + attr.getName());
						code.add("\t#ttype# #tmpname# = null;");
						code.add("\tif(#name# != null)");
						code.add("\t\t#tmpname# = new #ttype#(#name#);");
						code.setVariable("initLoading", "new "
								+ attr.getDomain().getVersionedClass(
										schemaRootPackageName)
								// TODO check: changed from myGraph to this!!!
								+ "(this, #tmpname#, \"#name#\");");
					}
				}

				code.add("\tif(#graphreference#isLoading())",
						"\t\tthis.#name# = #initLoading#",
						"\tif(this.#name# == null) {",
						"\t\tthis.#name# = #init#",
						"\t\tthis.#name#.setName(\"#name#\");", "\t}");

				if (domain.isComposite()) {
					// if (!(domain instanceof RecordDomainImpl)) {
					// code.add("\t\t#tmpname# = new #ttype#(#name#);");
					// }
					code.add("\tif(#tmpname# != null)");
					if (domain instanceof ListDomainImpl)
						code.add("\t\t#tmpname#.setVersionedList(this.#name#);");
					if (domain instanceof SetDomainImpl)
						code.add("\t\t#tmpname#.setVersionedSet(this.#name#);");
					if (domain instanceof MapDomainImpl)
						code.add("\t\t#tmpname#.setVersionedMap(this.#name#);");
					if (domain instanceof RecordDomainImpl)
						code.add("\t\t#name#.setVersionedRecord(this.#name#);");
				}
				code.add("\tthis.#name#.setValidValue(#tmpname#, #graphreference#getCurrentTransaction());",
						 "\tattributeChanged(this.#name#);",
						 "\tgraphModified();", "}");
			}
		} else {
			code.add("public void set#cName#(#type# #name#);");
		}
		return code;
	}

	protected CodeBlock createField(Attribute attr) {
		CodeSnippet code = new CodeSnippet(true, "protected #type# #name#;");
		code.setVariable("name", attr.getName());
		if (!transactionSupport)
			code.setVariable("type", attr.getDomain()
					.getJavaAttributeImplementationTypeName(
							schemaRootPackageName));
		else
			// versioned field
			code.setVariable("type", attr.getDomain().getVersionedClass(schemaRootPackageName));
		return code;
	}

	protected CodeBlock createReadAttributesFromStringMethod(
			Set<Attribute> attrSet) {
		CodeList code = new CodeList();

		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"public void readAttributeValueFromString(String _attributeName, String _value) throws GraphIOException, NoSuchFieldException {"));

		if (attrSet != null) {
			for (Attribute attribute : attrSet) {
				CodeList a = new CodeList();
				a.setVariable("variableName", attribute.getName());
				a.setVariable("setterName", "set"
						+ camelCase(attribute.getName()));
				a.addNoIndent(new CodeSnippet(
								"if (_attributeName.equals(\"#variableName#\")) {",
								"\tGraphIO _io = GraphIO.createStringReader(_value, getSchema());"));
				a.add(attribute.getDomain().getReadMethod(
						schemaRootPackageName, attribute.getName(), "_io"));
				if (transactionSupport) {
					a.addNoIndent(new CodeSnippet(
							"\t#setterName#(#variableName#.getLatestPersistentValue());", "\treturn;", "}"));	
				} else {
					a.addNoIndent(new CodeSnippet(
						"\t#setterName#(#variableName#);", "\treturn;", "}"));	
				}	
				code.add(a);
			}
		}
		code.add(new CodeSnippet(
						"throw new NoSuchFieldException(\"#qualifiedClassName# doesn't contain an attribute \" + _attributeName);"));
		code.addNoIndent(new CodeSnippet("}"));

		return code;
	}

	/**
	 * TODO: Check if the really the persistent values should be written when transaction support is enabled  
	 * @param attrSet
	 * @return
	 */
	protected CodeBlock createWriteAttributeToStringMethod(
			Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"public String writeAttributeValueToString(String _attributeName) throws IOException, GraphIOException, NoSuchFieldException {"));
		if (attrSet != null) {
			for (Attribute attribute : attrSet) {
				CodeList a = new CodeList();
				a.setVariable("variableName", attribute.getName());
				a.setVariable("setterName", "set"
						+ camelCase(attribute.getName()));
				a.addNoIndent(new CodeSnippet(
								"if (_attributeName.equals(\"#variableName#\")) {",
								"\tGraphIO _io = GraphIO.createStringWriter(getSchema());"));
				if (transactionSupport) {
					a.add(attribute.getDomain().getWriteMethod(
							schemaRootPackageName, attribute.getName()+ ".getLatestPersistentValue()", "_io"));
				} else {
					a.add(attribute.getDomain().getWriteMethod(
						schemaRootPackageName, attribute.getName(), "_io"));
				}	

				a.addNoIndent(new CodeSnippet(
					/*	"\t#setterName#(#variableName#);",*/
						"\treturn _io.getStringWriterResult();", "}"));
				code.add(a);
			}
		}
		code.add(new CodeSnippet(
						"throw new NoSuchFieldException(\"#qualifiedClassName# doesn't contain an attribute \" + _attributeName);"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	protected CodeBlock createReadAttributesMethod(Set<Attribute> attrSet) {
		CodeList code = new CodeList();

		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");

		code.addNoIndent(new CodeSnippet(true,
						"public void readAttributeValues(GraphIO io) throws GraphIOException {"));
		if (attrSet != null) {
			for (Attribute attribute : attrSet) {
				CodeSnippet snippet = new CodeSnippet();
				snippet.setVariable("setterName", "set"
						+ camelCase(attribute.getName()));
				if (!transactionSupport) {
					snippet.setVariable("variableName", attribute.getName());
					code.add(attribute.getDomain().getReadMethod(
							schemaRootPackageName, attribute.getName(), "io"));
				} else {
					// read-method for transaction support
					snippet.setVariable("variableName", "tmp"
							+ attribute.getName());
					code.add(attribute.getDomain().getTransactionReadMethod(
							schemaRootPackageName, attribute.getName(), "io"));
				}
				snippet.add("#setterName#(#variableName#);");
				code.add(snippet);
			}
		}
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	protected CodeBlock createWriteAttributesMethod(Set<Attribute> attrSet) {
		CodeList code = new CodeList();

		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException",
				"java.io.IOException");

		code.addNoIndent(new CodeSnippet(
						true,
						"public void writeAttributeValues(GraphIO io) throws GraphIOException, IOException {"));
		if (attrSet != null && !attrSet.isEmpty()) {
			code.add(new CodeSnippet("io.space();"));
			for (Attribute attribute : attrSet) {
				if (!transactionSupport)
					code.add(attribute.getDomain().getWriteMethod(
							schemaRootPackageName, attribute.getName(), "io"));
				else
					// write-method for transaction support
					code.add(attribute.getDomain().getTransactionWriteMethod(
							schemaRootPackageName, attribute.getName(), "io"));
			}
		}
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}
	
	/**
	 * Generates method attributes() which returns a set of all versioned
	 * attributes for an <code>AttributedElement</code>.
	 * 
	 * @param attributeList
	 * @return
	 */
	protected CodeBlock createGetVersionedAttributesMethod(
			SortedSet<Attribute> attributeList) {
		if (transactionSupport) {
			CodeSnippet code = new CodeSnippet();
			code.add("public java.util.Set<de.uni_koblenz.jgralab.trans.VersionedDataObject<?>> attributes() {");
			code.add("\tjava.util.Set<de.uni_koblenz.jgralab.trans.VersionedDataObject<?>> attributes = "
							+ "new java.util.HashSet<de.uni_koblenz.jgralab.trans.VersionedDataObject<?>>();");
			for (Attribute attribute : attributeList) {
				code.add("\tattributes.add(" + attribute.getName() + ");");
			}
			code.add("\treturn attributes;");
			code.add("}");
			return code;
		}
		return null;
	}

}
