/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralab.schema.codegenerator;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class AttributedElementCodeGenerator<SC extends AttributedElementClass<SC, IC>, IC extends AttributedElement<SC, IC>>
		extends CodeGenerator {

	/**
	 * all the interfaces of the class which are being implemented
	 */
	protected SortedSet<String> interfaces;

	/**
	 * the AttributedElementClass to generate code for
	 */
	protected SC aec;

	protected AttributedElementCodeGenerator(SC attributedElementClass,
			String schemaRootPackageName, CodeGeneratorConfiguration config) {
		super(schemaRootPackageName, attributedElementClass.getPackageName(),
				config);
		aec = attributedElementClass;
		rootBlock.setVariable("schemaTypeName", getSchemaTypeName());
		rootBlock.setVariable("qualifiedClassName", aec.getQualifiedName());
		rootBlock.setVariable("schemaName", aec.getSchema().getName());
		rootBlock.setVariable("javaClassName", schemaRootPackageName + "."
				+ aec.getQualifiedName());
		rootBlock.setVariable("qualifiedImplClassName", schemaRootPackageName
				+ ".impl.std" + aec.getQualifiedName() + "Impl");
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
	}

	protected abstract String getSchemaTypeName();

	/**
	 * Returns the absolute name of the given AttributdelementClass. The name is
	 * composed of the package-prefix of the schema the class belongs to and the
	 * qualified name of the class
	 * 
	 * @param aec
	 * @return
	 */
	protected String absoluteName(AttributedElementClass<?, ?> aec) {
		return schemaRootPackageName + "." + aec.getQualifiedName();
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = new CodeList();
		if (currentCycle.isStdOrDiskv2Impl()){//currentCycle.isStdImpl()) {
			code.add(createFields(aec.getAttributeList()));
			code.add(createConstructor());
			code.add(createGetAttributedElementClassMethod());
			code.add(createGetSchemaClassMethod());
			code.add(createGenericGetter(aec.getAttributeList()));
			code.add(createGenericSetter(aec.getAttributeList()));
			code.add(createGettersAndSetters(aec.getAttributeList()));
			code.add(createReadAttributesMethod(aec.getAttributeList()));
			code.add(createReadAttributesFromStringMethod(aec
					.getAttributeList()));
			code.add(createWriteAttributesMethod(aec.getAttributeList()));
			code.add(createWriteAttributeToStringMethod(aec.getAttributeList()));
		}
		if (currentCycle.isAbstract()) {
			code.add(createAttributedElementClassConstant());
			code.add(createGettersAndSetters(aec.getOwnAttributeList()));
		}
		return code;
	}

	protected abstract CodeBlock createAttributedElementClassConstant();

	@Override
	protected CodeBlock createHeader() {
		CodeSnippet code = new CodeSnippet(true);

		code.setVariable("classOrInterface",
				currentCycle.isStdOrDiskv2Impl() ? " class" : " interface");
				//currentCycle.isStdImpl() ? " class" : " interface");
		code.setVariable("abstract",
				currentCycle.isStdOrDiskv2Impl() && aec.isAbstract() ? " abstract" : "");
				//currentCycle.isStdImpl() && aec.isAbstract() ? " abstract" : "");
		code.setVariable("impl",
				currentCycle.isStdOrDiskv2Impl() && !aec.isAbstract() ? "Impl" : "");
				//currentCycle.isStdImpl() && !aec.isAbstract() ? "Impl" : "");
		code.add("public#abstract##classOrInterface# #simpleClassName##impl##extends##implements# {");
		code.setVariable("extends",
				currentCycle.isStdOrDiskv2Impl() ? " extends #baseClassName#" : "");
				//currentCycle.isStdImpl() ? " extends #baseClassName#" : "");


		StringBuilder buf = new StringBuilder();
		if (interfaces.size() > 0) {
			//String delim = currentCycle.isStdImpl() ? " implements "
			String delim = currentCycle.isStdOrDiskv2Impl() ? " implements "		
					: " extends ";
			for (String interfaceName : interfaces) {
				if (currentCycle.isStdOrDiskv2Impl()//.isStdImpl()
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

	protected abstract CodeBlock createGetAttributedElementClassMethod();

	protected CodeBlock createGetSchemaClassMethod() {
		return new CodeSnippet(
				true,
				"@Override",
				"public final java.lang.Class<? extends #jgPackage#.#graphElementClass#> getSchemaClass() {",
				"\treturn #javaClassName#.class;", "}");
	}

	protected CodeBlock createGenericGetter(List<Attribute> attributes) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.exception.NoSuchAttributeException");
		CodeSnippet snip = new CodeSnippet(true);
		code.addNoIndent(snip);
		if (!attributes.isEmpty()) {
			snip.add("@SuppressWarnings(\"unchecked\")");
		}
		snip.add("public <T> T getAttribute(String attributeName) {");
		for (Attribute attr : attributes) {
			CodeSnippet s = new CodeSnippet();
			if (attr.getDomain().isPrimitive()) {
				s.setVariable(
						"attributeClassName",
						" ("
								+ attr.getDomain().getJavaClassName(
										schemaRootPackageName) + ")");
			} else {
				s.setVariable("attributeClassName", "");
			}
			s.setVariable("name", attr.getName());
			s.setVariable("isOrGet", attr.getDomain().isBoolean() ? "is"
					: "get");
			s.setVariable("cName", attr.getName());
			s.add("if (attributeName.equals(\"#name#\")) return (T)#attributeClassName# #isOrGet#_#cName#();");
			code.add(s);
		}
		code.add(new CodeSnippet(
				"throw new NoSuchAttributeException(\"#qualifiedClassName# doesn't contain an attribute \" + attributeName);"));
		code.addNoIndent(new CodeSnippet("}"));

		return code;
	}

	protected CodeBlock createGenericSetter(List<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.exception.NoSuchAttributeException");
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
		snip.add("public <T> void setAttribute(String attributeName, T data) {");
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

	protected CodeBlock createFields(List<Attribute> attributes) {
		CodeList code = new CodeList();
		for (Attribute attr : attributes) {
			code.addNoIndent(createField(attr));
		}
		return code;
	}

	protected CodeBlock createGettersAndSetters(List<Attribute> attributes) {
		CodeList code = new CodeList();
		for (Attribute attr : attributes) {
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
		code.setVariable("isOrGet", attr.getDomain().isBoolean() ? "is" : "get");

		switch (currentCycle) {
		case ABSTRACT:
			code.add("public #type# #isOrGet#_#name#();");
			break;
		case STDIMPL:
			code.add("public #type# #isOrGet#_#name#() {", "\treturn _#name#;",
					"}");
			break;
		case DISKV2IMPL:
			code.add("public #type# #isOrGet#_#name#() {", "\treturn _#name#;",
					"}");
		case CLASSONLY:
			break;
		}
		return code;
	}

	protected void addCheckValidityCode(CodeSnippet code) {
		code.add(
				"\tif (!isValid())",
				"\t\tthrow new #jgPackage#.exception.GraphException(\"Cannot access attribute '#name#', because \" + this + \" isn't valid in current transaction.\");");
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
			code.add(
					"public void set_#name#(#type# _#name#) {",
					"\tecaAttributeChanging(\"#name#\", this._#name#, _#name#);",
					"\tObject oldValue = this._#name#;",
					"\tthis._#name# = _#name#;", "\tgraphModified();",
					"\tecaAttributeChanged(\"#name#\", oldValue, _#name#);",
					"}");
			break;
		case DISKV2IMPL:
			code.add(
					"public void set_#name#(#type# _#name#) {",
					"\tecaAttributeChanging(\"#name#\", this._#name#, _#name#);",
					"\tObject oldValue = this._#name#;",
					"\tthis._#name# = _#name#;", "\tgraphModified();",
					"\tecaAttributeChanged(\"#name#\", oldValue, _#name#);",
					"\tattributeChanged();",
					"}");
		case CLASSONLY:
			break;
		}
		return code;
	}

	protected CodeBlock createField(Attribute attr) {
		CodeSnippet code = new CodeSnippet(true, "protected #type# _#name#;");
		code.setVariable("name", attr.getName());
		if (currentCycle.isStdOrDiskv2Impl()){//.isStdImpl()) {
			code.setVariable(
					"type",
					attr.getDomain().getJavaAttributeImplementationTypeName(
							schemaRootPackageName));
		}
		return code;
	}

	protected CodeBlock createReadAttributesFromStringMethod(
			List<Attribute> attributes) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO",
				"#jgPackage#.exception.GraphIOException",
				"#jgPackage#.exception.NoSuchAttributeException");
		code.addNoIndent(new CodeSnippet(
				true,
				"public void readAttributeValueFromString(String attributeName, String value) throws GraphIOException {"));

		if (attributes != null) {
			for (Attribute attribute : attributes) {
				CodeList a = new CodeList();
				a.setVariable("variableName", attribute.getName());
				a.setVariable("setterName", "set_" + attribute.getName());
				a.addNoIndent(new CodeSnippet(
						"if (attributeName.equals(\"#variableName#\")) {",
						"\tGraphIO io = GraphIO.createStringReader(value, getSchema());"));
				if (currentCycle.isStdOrDiskv2Impl()){//.isStdImpl()) {
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
	 * @param attributes
	 * @return
	 */
	protected CodeBlock createWriteAttributeToStringMethod(
			List<Attribute> attributes) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO",
				"#jgPackage#.exception.GraphIOException",
				"#jgPackage#.exception.NoSuchAttributeException");
		code.addNoIndent(new CodeSnippet(
				true,
				"public String writeAttributeValueToString(String attributeName) throws IOException, GraphIOException {"));
		if (attributes != null) {
			for (Attribute attribute : attributes) {
				CodeList a = new CodeList();
				a.setVariable("variableName", attribute.getName());
				a.setVariable("setterName", "set_" + attribute.getName());
				a.addNoIndent(new CodeSnippet(
						"if (attributeName.equals(\"#variableName#\")) {",
						"\tGraphIO io = GraphIO.createStringWriter(getSchema());"));
				if (currentCycle.isStdOrDiskv2Impl()){//.isStdImpl()) {
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

	protected CodeBlock createReadAttributesMethod(List<Attribute> attributes) {
		CodeList code = new CodeList();

		addImports("#jgPackage#.GraphIO",
				"#jgPackage#.exception.GraphIOException");

		code.addNoIndent(new CodeSnippet(true,
				"public void readAttributeValues(GraphIO io) throws GraphIOException {"));
		if (attributes != null) {
			for (Attribute attribute : attributes) {
				CodeSnippet snippet = new CodeSnippet();
				snippet.setVariable("setterName", "set_" + attribute.getName());
				snippet.setVariable("variableName", attribute.getName());
				if (currentCycle.isStdOrDiskv2Impl()){//.isStdImpl()) {
					code.add(attribute.getDomain().getReadMethod(
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

	protected CodeBlock createWriteAttributesMethod(List<Attribute> attributes) {
		CodeList code = new CodeList();

		addImports("#jgPackage#.GraphIO",
				"#jgPackage#.exception.GraphIOException", "java.io.IOException");

		code.addNoIndent(new CodeSnippet(
				true,
				"public void writeAttributeValues(GraphIO io) throws GraphIOException, IOException {"));
		if ((attributes != null) && !attributes.isEmpty()) {
			code.add(new CodeSnippet("io.space();"));
			for (Attribute attribute : attributes) {
				if (currentCycle.isStdOrDiskv2Impl()){//.isStdImpl()) {
					code.add(attribute.getDomain().getWriteMethod(
							schemaRootPackageName, "_" + attribute.getName(),
							"io"));
				}
			}
		}
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}
}
