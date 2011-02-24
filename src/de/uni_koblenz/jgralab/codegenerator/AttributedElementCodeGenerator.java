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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;

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
			String schemaRootPackageName, CodeGeneratorConfiguration config) {
		super(schemaRootPackageName, attributedElementClass.getPackageName(),
				config);
		aec = attributedElementClass;
		rootBlock.setVariable("ecName", aec.getSimpleName());
		rootBlock.setVariable("qualifiedClassName", aec.getQualifiedName());
		rootBlock.setVariable("schemaName", aec.getSchema().getName());
		rootBlock.setVariable("schemaVariableName", aec.getVariableName());
		rootBlock.setVariable("javaClassName", schemaRootPackageName + "."
				+ aec.getQualifiedName());
		rootBlock.setVariable("qualifiedImplClassName", schemaRootPackageName
				+ ".impl." + (config.hasTransactionSupport() ? "trans" : "std")
				+ aec.getQualifiedName() + "Impl");
		rootBlock.setVariable("simpleClassName", aec.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", aec.getSimpleName()
				+ "Impl");
		rootBlock.setVariable("uniqueClassName", aec.getUniqueName());
		rootBlock.setVariable("schemaPackageName", schemaRootPackageName);
		rootBlock.setVariable("theGraph", "graph");

		interfaces = new TreeSet<String>();
		interfaces.add(aec.getQualifiedName());
		rootBlock.setVariable("isAbstractClass", aec.isAbstract() ? "true"
				: "false");
		for (AttributedElementClass superClass : attributedElementClass
				.getDirectSuperClasses()) {
			interfaces.add(superClass.getQualifiedName());
		}
	}

	/**
	 * Returns the absolute name of the given AttributdelementClass. The name is
	 * composed of the package-prefix of the schema the class belongs to and the
	 * qualified name of the class
	 * 
	 * @param aec
	 * @return
	 */
	protected String absoluteName(AttributedElementClass aec) {
		return schemaRootPackageName + "." + aec.getQualifiedName();
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()) {
			code.add(createFields(aec.getAttributeList()));
			code.add(createConstructor());
			code.add(createGetAttributedElementClassMethod());
			code.add(createGetM1ClassMethod());
			code.add(createGenericGetter(aec.getAttributeList()));
			code.add(createGenericSetter(aec.getAttributeList()));
			code.add(createGettersAndSetters(aec.getAttributeList()));
			code.add(createReadAttributesMethod(aec.getAttributeList()));
			code.add(createReadAttributesFromStringMethod(aec
					.getAttributeList()));
			code.add(createWriteAttributesMethod(aec.getAttributeList()));
			code.add(createWriteAttributeToStringMethod(aec.getAttributeList()));
			code.add(createGetVersionedAttributesMethod(aec.getAttributeList()));
		}
		if (currentCycle.isAbstract()) {
			code.add(createGettersAndSetters(aec.getOwnAttributeList()));
		}
		return code;
	}

	@Override
	protected CodeBlock createHeader() {
		CodeSnippet code = new CodeSnippet(true);

		code.setVariable("classOrInterface", currentCycle
				.isStdOrSaveMemOrDbImplOrTransImpl() ? " class" : " interface");
		code.setVariable(
				"abstract",
				currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()
						&& aec.isAbstract() ? " abstract" : "");
		code.setVariable(
				"impl",
				currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()
						&& !aec.isAbstract() ? "Impl" : "");
		code.add("public#abstract##classOrInterface# #simpleClassName##impl##extends##implements# {");
		code.setVariable(
				"extends",
				currentCycle.isStdOrSaveMemOrDbImplOrTransImpl() ? " extends #baseClassName#"
						: "");

		StringBuffer buf = new StringBuffer();
		if (interfaces.size() > 0) {
			String delim = currentCycle.isStdOrSaveMemOrDbImplOrTransImpl() ? " implements "
					: " extends ";
			for (String interfaceName : interfaces) {
				if (currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()
						|| !interfaceName.equals(aec.getQualifiedName())) {
					if (interfaceName.equals("Vertex")
							|| interfaceName.equals("Edge")
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
		if (hasDefaultAttributeValues()) {
			code.addNoIndent(new CodeSnippet(
					"\tinitializeAttributesWithDefaultValues();"));
		}

		code.add(createSpecialConstructorCode());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	/**
	 * @return true if at least one own or inherited attribute has a default
	 *         value.
	 */
	protected boolean hasDefaultAttributeValues() {
		for (Attribute attr : aec.getAttributeList()) {
			if (attr.getDefaultValueAsString() != null) {
				return true;
			}
		}
		return false;
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
		addImports("#jgPackage#.NoSuchAttributeException");
		code.addNoIndent(new CodeSnippet(true,
				"public Object getAttribute(String attributeName) {"));
		for (Attribute attr : attrSet) {
			CodeSnippet s = new CodeSnippet();
			s.setVariable("name", attr.getName());
			s.setVariable("isOrGet",
					attr.getDomain().getJavaClassName(schemaRootPackageName)
							.equals("Boolean") ? "is" : "get");
			s.setVariable("cName", attr.getName());
			s.add("if (attributeName.equals(\"#name#\")) return #isOrGet#_#cName#();");
			code.add(s);
		}
		code.add(new CodeSnippet(
				"throw new NoSuchAttributeException(\"#qualifiedClassName# doesn't contain an attribute \" + attributeName);"));
		code.addNoIndent(new CodeSnippet("}"));

		return code;
	}

	protected CodeBlock createGenericSetter(Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.NoSuchAttributeException");
		CodeSnippet snip = new CodeSnippet(true);
		boolean suppressWarningsNeeded = false;
		for (Attribute attr : attrSet) {
			if (attr.getDomain().isComposite()
					&& !(attr.getDomain() instanceof RecordDomain)) {
				suppressWarningsNeeded = true;
				break;
			}
		}
		if (suppressWarningsNeeded) {
			snip.add("@SuppressWarnings(\"unchecked\")");
		}
		snip.add("public void setAttribute(String attributeName, Object data) {");
		code.addNoIndent(snip);
		for (Attribute attr : attrSet) {
			CodeSnippet s = new CodeSnippet();
			s.setVariable("name", attr.getName());

			if (attr.getDomain().isComposite()) {
				s.setVariable(
						"attributeClassName",
						attr.getDomain()
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
				s.add("\t\tset_#name#(#attributeClassName#.valueOfPermitNull((String) data));");
				s.add("\t} else {");
				s.add("\t\tset_#name#((#attributeClassName#) data);");

				// if(currentCycle.isDbImpl())
				// s.add("\tsuper.setAttribute(attributeName, data);");
				s.add("\t}");
				s.add("\treturn;");
				s.add("}");
			} else {
				s.add("if (attributeName.equals(\"#name#\")) {");
				s.add("\tset_#name#((#attributeClassName#) data);");

				// if(currentCycle.isDbImpl())
				// s.add("\tsuper.setAttribute(attributeName, data);");
				s.add("\treturn;");
				s.add("}");
			}
			code.add(s);
		}
		code.add(new CodeSnippet(
				"throw new NoSuchAttributeException(\"#qualifiedClassName# doesn't contain an attribute \" + attributeName);"));
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

	protected CodeBlock createGettersAndSetters(Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		for (Attribute attr : attrSet) {
			code.addNoIndent(createGetter(attr));
			code.addNoIndent(createSetter(attr));
		}
		return code;
	}

	protected CodeBlock createGetter(Attribute attr) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", attr.getName());
		code.setVariable("type", attr.getDomain()
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));
		code.setVariable("isOrGet",
				attr.getDomain().getJavaClassName(schemaRootPackageName)
						.equals("Boolean") ? "is" : "get");

		switch (currentCycle) {
		case ABSTRACT:
			code.add("public #type# #isOrGet#_#name#();");
			break;
		case STDIMPL:
		case DBIMPL:
		case SAVEMEMIMPL:
			code.add("public #type# #isOrGet#_#name#() {", "\treturn _#name#;",
					"}");
			break;
		case TRANSIMPL:
			code.setVariable("initValue", attr.getDomain().getInitialValue());
			code.setVariable(
					"ttype",
					attr.getDomain()
							.getTransactionJavaAttributeImplementationTypeName(
									schemaRootPackageName));

			code.add("public #type# #isOrGet#_#name#() {");
			addCheckValidityCode(code);
			code.add("\tif (_#name# == null)", "\t\treturn #initValue#;",
					"\t#ttype# value = _#name#.getValidValue(#theGraph#.getCurrentTransaction());");

			if (attr.getDomain().isComposite()) {
				code.add("\tif(_#name# != null && value != null)");
				code.add("\t\tvalue.setName(this + \":#name#\");");
			}
			code.add("\treturn (value == null) ? #initValue# : value;", "}");
			break;
		}
		return code;
	}

	protected void addCheckValidityCode(CodeSnippet code) {
		code.add(
				"\tif (!isValid())",
				"\t\tthrow new #jgPackage#.GraphException(\"Cannot access attribute '#name#', because \" + this + \" isn't valid in current transaction.\");");
	}

	protected CodeBlock createSetter(Attribute attr) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", attr.getName());
		code.setVariable("type", attr.getDomain()
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));
		code.setVariable("dname", attr.getDomain().getSimpleName());

		switch (currentCycle) {
		case ABSTRACT:
			code.add("public void set_#name#(#type# _#name#);");
			break;
		case STDIMPL:

		case SAVEMEMIMPL:
			code.add("public void set_#name#(#type# _#name#) {",
					"\tthis._#name# = _#name#;", "\tgraphModified();", "}");
			break;
		case DBIMPL:
			code.add("public void set_#name#(#type# _#name#) {");
			code.add("\tthis._#name# = _#name#;");
			// code.add("\tif(this.isInitialized())");
			// code.add("\t\tgraphModified();");
			code.add("\tattributeChanged(\"#name#\");");
			code.add("}");
			break;
		case TRANSIMPL:
			Domain domain = attr.getDomain();
			// setter for transaction support
			code.setVariable(
					"ttype",
					attr.getDomain()
							.getTransactionJavaAttributeImplementationTypeName(
									schemaRootPackageName));
			code.setVariable("vclass",
					attr.getDomain().getVersionedClass(schemaRootPackageName));

			if (!(domain instanceof RecordDomain)) {
				code.setVariable("initLoading",
						"new #vclass#(this, _#name#, \"#name#\");");
			} else {
				code.setVariable("initLoading",
						"new #vclass#(this, (#ttype) _#name#, \"#name#\");");
			}
			code.setVariable("init", "new #vclass#(this);");

			code.add("public void set_#name#(#type# _#name#) {");
			addCheckValidityCode(code);

			if (domain.isComposite()) {
				addImports("#jgTransPackage#.JGraLabTransactionCloneable");
				addImports("#jgPackage#.GraphException");
				code.setVariable("tclassname", attr.getDomain()
						.getTransactionJavaClassName(schemaRootPackageName));
				code.add("\tif(_#name# != null && !(_#name# instanceof #jgTransPackage#.JGraLabTransactionCloneable))");
				code.add("\t\tthrow new GraphException(\"The given parameter of type #dname# doesn't support transactions.\");");
				code.add("\tif(_#name# != null && ((#jgTransPackage#.JGraLabTransactionCloneable)_#name#).getGraph() != #theGraph#)");
				code.add("\t\tthrow new GraphException(\"The given parameter of type #dname# belongs to another graph.\");");
				code.setVariable("initLoading",
						"new #vclass#(this, (#ttype#) _#name#, \"#name#\");");
			}

			code.add("\tif(#theGraph#.isLoading())",
					"\t\tthis._#name# = #initLoading#",
					"\tif(this._#name# == null) {",
					"\t\tthis._#name# = #init#",
					"\t\tthis._#name#.setName(\"#name#\");", "\t}");

			if (domain.isComposite()) {
				code.add("\tif(_#name# != null)");
				code.add("\t((JGraLabTransactionCloneable)_#name#).setName(this + \":#name#\");");
			}
			code.add(
					"\tthis._#name#.setValidValue((#ttype#) _#name#, #theGraph#.getCurrentTransaction());",
					"\tattributeChanged(this._#name#);", "\tgraphModified();",
					"}");
			break;
		}
		return code;
	}

	protected CodeBlock createField(Attribute attr) {
		CodeSnippet code = new CodeSnippet(true, "protected #type# _#name#;");
		code.setVariable("name", attr.getName());
		if (currentCycle.isStdImpl() || currentCycle.isSaveMemImpl()
				|| currentCycle.isDbImpl()) {
			code.setVariable(
					"type",
					attr.getDomain().getJavaAttributeImplementationTypeName(
							schemaRootPackageName));
		}
		if (currentCycle.isTransImpl()) {
			code.setVariable("type",
					attr.getDomain().getVersionedClass(schemaRootPackageName));
		}
		return code;
	}

	protected CodeBlock createReadAttributesFromStringMethod(
			Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException",
				"#jgPackage#.NoSuchAttributeException");
		code.addNoIndent(new CodeSnippet(
				true,
				"public void readAttributeValueFromString(String attributeName, String value) throws GraphIOException {"));

		if (attrSet != null) {
			for (Attribute attribute : attrSet) {
				CodeList a = new CodeList();
				a.setVariable("variableName", attribute.getName());
				a.setVariable("setterName", "set_" + attribute.getName());
				a.addNoIndent(new CodeSnippet(
						"if (attributeName.equals(\"#variableName#\")) {",
						"\tGraphIO io = GraphIO.createStringReader(value, getSchema());"));
				if (currentCycle.isTransImpl()) {
					CodeSnippet readBlock = new CodeSnippet();
					readBlock.setVariable("variableType", attribute.getDomain()
							.getJavaClassName(schemaRootPackageName));
					readBlock.add("#variableType# tmpVar = null;");
					a.add(readBlock);
					a.add(attribute.getDomain().getReadMethod(
							schemaRootPackageName, "tmpVar", "io"));
					a.addNoIndent(new CodeSnippet("\t#setterName#(tmpVar);",
							"\treturn;", "}"));
				}
				if (currentCycle.isStdImpl() || currentCycle.isSaveMemImpl()
						|| currentCycle.isDbImpl()) {
					a.add(attribute.getDomain().getReadMethod(
							schemaRootPackageName, "_" + attribute.getName(),
							"io"));
					a.addNoIndent(new CodeSnippet(
							"\t#setterName#(_#variableName#);", "\treturn;",
							"}"));
				}
				code.add(a);
			}
		}
		code.add(new CodeSnippet(
				"throw new NoSuchAttributeException(\"#qualifiedClassName# doesn't contain an attribute \" + attributeName);"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	/**
	 * 
	 * @param attrSet
	 * @return
	 */
	protected CodeBlock createWriteAttributeToStringMethod(
			Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException",
				"#jgPackage#.NoSuchAttributeException");
		code.addNoIndent(new CodeSnippet(
				true,
				"public String writeAttributeValueToString(String attributeName) throws IOException, GraphIOException {"));
		if (attrSet != null) {
			for (Attribute attribute : attrSet) {
				CodeList a = new CodeList();
				a.setVariable("variableName", attribute.getName());
				a.setVariable("setterName", "set_" + attribute.getName());
				a.addNoIndent(new CodeSnippet(
						"if (attributeName.equals(\"#variableName#\")) {",
						"\tGraphIO io = GraphIO.createStringWriter(getSchema());"));
				if (currentCycle.isTransImpl()) {
					a.add(attribute.getDomain().getTransactionWriteMethod(
							schemaRootPackageName, "_" + attribute.getName(),
							"io"));
				}
				if (currentCycle.isStdImpl() || currentCycle.isSaveMemImpl()
						|| currentCycle.isDbImpl()) {
					a.add(attribute.getDomain().getWriteMethod(
							schemaRootPackageName, "_" + attribute.getName(),
							"io"));
				}
				a.addNoIndent(new CodeSnippet(
						"\treturn io.getStringWriterResult();", "}"));
				code.add(a);
			}
		}
		code.add(new CodeSnippet(
				"throw new NoSuchAttributeException(\"#qualifiedClassName# doesn't contain an attribute \" + attributeName);"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	protected CodeBlock createReadAttributesMethod(SortedSet<Attribute> attrSet) {
		CodeList code = new CodeList();

		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");

		code.addNoIndent(new CodeSnippet(true,
				"public void readAttributeValues(GraphIO io) throws GraphIOException {"));
		if (attrSet != null) {
			for (Attribute attribute : attrSet) {
				CodeSnippet snippet = new CodeSnippet();
				snippet.setVariable("setterName", "set_" + attribute.getName());
				snippet.setVariable("variableName", attribute.getName());
				if (currentCycle.isStdImpl() || currentCycle.isSaveMemImpl()
						|| currentCycle.isDbImpl()) {
					code.add(attribute.getDomain().getReadMethod(
							schemaRootPackageName, "_" + attribute.getName(),
							"io"));
				}
				if (currentCycle.isTransImpl()) {
					code.add(attribute.getDomain().getTransactionReadMethod(
							schemaRootPackageName, "_" + attribute.getName(),
							"io"));
				}
				snippet.add("#setterName#(_#variableName#);");
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
		if ((attrSet != null) && !attrSet.isEmpty()) {
			code.add(new CodeSnippet("io.space();"));
			for (Attribute attribute : attrSet) {
				if (currentCycle.isStdImpl() || currentCycle.isSaveMemImpl()
						|| currentCycle.isDbImpl()) {
					code.add(attribute.getDomain().getWriteMethod(
							schemaRootPackageName, "_" + attribute.getName(),
							"io"));
				}
				if (currentCycle.isTransImpl()) {
					code.add(attribute.getDomain().getTransactionWriteMethod(
							schemaRootPackageName, "_" + attribute.getName(),
							"io"));
				}
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
		CodeList code = new CodeList();
		if (currentCycle.isTransImpl()) {
			CodeSnippet codeSnippet = new CodeSnippet();
			codeSnippet
					.add("public java.util.Set<#jgTransPackage#.VersionedDataObject<?>> attributes() {");
			codeSnippet
					.add("\tjava.util.Set<#jgTransPackage#.VersionedDataObject<?>> attributes = "
							+ "new java.util.HashSet<#jgTransPackage#.VersionedDataObject<?>>();");
			code.addNoIndent(codeSnippet);
			for (Attribute attribute : attributeList) {
				codeSnippet = new CodeSnippet("\tattributes.add(_#aname#);");
				codeSnippet.setVariable("aname", attribute.getName());
				code.addNoIndent(codeSnippet);
			}
			code.addNoIndent(new CodeSnippet("\treturn attributes;"));
			code.addNoIndent(new CodeSnippet("}"));
			return code;
		}
		return code;
	}
}
