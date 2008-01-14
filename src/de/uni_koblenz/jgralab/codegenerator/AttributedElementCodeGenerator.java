/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.Domain;
import de.uni_koblenz.jgralab.EnumDomain;
import de.uni_koblenz.jgralab.ListDomain;
import de.uni_koblenz.jgralab.RecordDomain;
import de.uni_koblenz.jgralab.SetDomain;

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
			String schemaPackageName, String implementationName) {
		super(schemaPackageName, implementationName);
		aec = attributedElementClass;
		rootBlock.setVariable("className", aec.getName());
		rootBlock.setVariable("implClassName", aec.getName() + "Impl");
		interfaces = new TreeSet<String>();
		interfaces.add(aec.getName());
		rootBlock.setVariable("isAbstractClass", aec.isAbstract() ? "true"
				: "false");
		for (AttributedElementClass superClass : attributedElementClass
				.getAllSuperClasses()) {
			interfaces.add(superClass.getName());
		}
		if (interfaces.contains("Aggregation")) {
			interfaces.remove("Edge");
		}
		if (interfaces.contains("Composition")) {
			interfaces.remove("Aggregation");
		}
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = new CodeList();
		if (!aec.isAbstract() && !createClass) {
			code.add(createStaticImplementationClassField());
		}
		if (createClass) {
			code.add(createFields(aec.getAttributeList()));
			code.add(createConstructor());
			code.add(createGetM1ClassMethod());
			code.add(createGenericGetter(aec.getAttributeList()));
			code.add(createGenericSetter(aec.getAttributeList()));
			code.add(createGettersAndSetters(aec.getAttributeList(),
					createClass));
			code.add(createReadAttributesMethod(aec.getAttributeList()));
			code.add(createWriteAttributesMethod(aec.getAttributeList()));
		} else {
			code.add(createGettersAndSetters(aec.getOwnAttributeList(),
					createClass));
		}
		return code;
	}

	protected CodeBlock createHeader(boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("classOrInterface", createClass ? " class"
				: " interface");
		code.setVariable("abstract",
				createClass && aec.isAbstract() ? " abstract" : "");
		code
				.setVariable("impl", createClass && !aec.isAbstract() ? "Impl"
						: "");

		code
				.add("public#abstract##classOrInterface# #className##impl##extends##implements# {");

		code.setVariable("extends", createClass ? " extends #baseClassName#"
				: "");

		StringBuffer buf = new StringBuffer();
		if (interfaces.size() > 0) {
			String delim = createClass ? " implements " : " extends ";
			for (String interfaceName : interfaces) {
				if (createClass || !interfaceName.equals(aec.getName())) {
					if (interfaceName.equals("Vertex")
							|| interfaceName.equals("Edge")
							|| interfaceName.equals("Aggregation")
							|| interfaceName.equals("Composition")
							|| interfaceName.equals("Graph")) {
						addImports("#jgPackage#." + interfaceName);
					} else {
						if (createClass)
							addImports("#schemaPackage#." + interfaceName);
					}
					buf.append(delim);
					buf.append(interfaceName);
					delim = ", ";
				}
			}
		}
		code.setVariable("implements", buf.toString());
		return code;
	}

	protected CodeBlock createStaticImplementationClassField() {
		addImports("#schemaImplPackage#.#className#Impl");
		return new CodeSnippet(
				true,
				"/**",
				" * refers to the default implementation class of this interface",
				" */",
				"public static final Class<#className#Impl> IMPLEMENTATION_CLASS = #className#Impl.class;");
	}

	protected CodeBlock createSpecialConstructorCode() {
		return null;
	}

	protected CodeBlock createConstructor() {
		if (!rootBlock.getVariable("graphElementClass").equals("ReversedEdge")) {
			addImports("#jgPackage#.#graphElementClass#Class",
					"#jgPackage#.Graph");
		}

		CodeList code = new CodeList();
		code
				.addNoIndent(new CodeSnippet(
						true,
						"public #className#Impl(int id, Graph g) {",
						"\tsuper(id, g, (#graphElementClass#Class)g.getGraphClass().getGraphElementClass(\"#className#\"));"));
		code.add(createSpecialConstructorCode());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	protected CodeBlock createGetM1ClassMethod() {
		addImports("#jgPackage#.AttributedElement");
		return new CodeSnippet(true, "public Class<? extends AttributedElement> getM1Class() {",
				"\treturn #className#.class;", "}");
	}

	protected CodeBlock createGenericGetter(Set<Attribute> attrSet) {
		CodeList code = new CodeList();

		code
				.addNoIndent(new CodeSnippet(
						true,
						"public Object getAttribute(String attributeName) throws NoSuchFieldException {"));
		for (Attribute attr : attrSet) {
			CodeSnippet s = new CodeSnippet();
			s.setVariable("name", attr.getName());
			s.add("if (attributeName.equals(\"#name#\")) return #name#;");
			code.add(s);
		}
		code
				.add(new CodeSnippet(
						"throw new NoSuchFieldException(\"#className# doesn't contain an attribute \" + attributeName);"));
		code.addNoIndent(new CodeSnippet("}"));

		return code;
	}
	
	protected CodeBlock createGenericSetter(Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		CodeSnippet snip = new CodeSnippet(true);
		snip.add("@SuppressWarnings(\"unchecked\")");
		snip.add("public void setAttribute(String attributeName, Object data) throws NoSuchFieldException {");
		code.addNoIndent(snip);
		for (Attribute attr : attrSet) {
			CodeSnippet s = new CodeSnippet();
			s.setVariable("name", attr.getName());
			s.setVariable("cName", camelCase(attr.getName()));
			s.setVariable("attributeClassName", attr.getDomain().getJavaClassName());
			s.add("if (attributeName.equals(\"#name#\")) {");
			s.add("\tset#cName#((#attributeClassName#) data);");
			s.add("\treturn;");
			s.add("}");
			code.add(s);
		}
		code.add(new CodeSnippet(
						"throw new NoSuchFieldException(\"#className# doesn't contain an attribute \" + attributeName);"));
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
				.getJavaAttributeImplementationTypeName());
		code.setVariable("isOrGet", attr.getDomain().getJavaClassName().equals(
				"Boolean") ? "is" : "get");


		if (createClass) {
			addDomainImport(attr);
			code.add("public #type# #isOrGet##cName#() {", "\treturn #name#;",
					"}");
		} else {
			code.add("public #type# #isOrGet##cName#();");
		}
		return code;
	}

	protected CodeBlock createSetter(Attribute attr, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", attr.getName());
		code.setVariable("cName", camelCase(attr.getName()));
		code.setVariable("type", attr.getDomain()
				.getJavaAttributeImplementationTypeName());

		if (createClass) {
			addDomainImport(attr);
			code.add("public void set#cName#(#type# #name#) {",
					"\tthis.#name# = #name#;", "\tmodified();", "}");
		} else {
			code.add("public void set#cName#(#type# #name#);");
		}
		return code;
	}

	protected CodeBlock createField(Attribute attr) {
		CodeSnippet code = new CodeSnippet(true, "protected #type# #name#;");
		code.setVariable("name", attr.getName());
		code.setVariable("type", attr.getDomain()
				.getJavaAttributeImplementationTypeName());
		addDomainImport(attr);
		return code;
	}

	protected void addDomainImport(Attribute attr) {
		Domain d = attr.getDomain();
		// if d is a set or a list domain, descend into component 
		while (d instanceof SetDomain || d instanceof ListDomain) {
			if (d instanceof SetDomain) {
				SetDomain sd = (SetDomain) d;
				d = sd.getBaseDomain();
			} else {
				ListDomain ld = (ListDomain) d;
				d = ld.getBaseDomain();
			}
		}
		
		// add imports for Enum/Record domains
		if (d instanceof EnumDomain || d instanceof RecordDomain) {
			addImports("#schemaPackage#." + d.getName());
		}
	}

	protected CodeBlock createReadAttributesMethod(Set<Attribute> attrSet) {
		CodeList code = new CodeList();

		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");

		code.addNoIndent(new CodeSnippet(true,
						"public void readAttributeValues(GraphIO io) throws GraphIOException {"));
		if (attrSet != null) {
			for (Attribute attribute : attrSet) {
				addDomainImport(attribute);
				code.add(attribute.getDomain().getReadMethod(
						attribute.getName(), "io"));
				CodeSnippet snippet = new CodeSnippet();
				snippet.setVariable("setterName", "set" + camelCase(attribute.getName()));
				snippet.setVariable("variableName", attribute.getName());
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

		code
				.addNoIndent(new CodeSnippet(
						true,
						"public void writeAttributeValues(GraphIO io) throws GraphIOException, IOException {"));
		if (attrSet != null && !attrSet.isEmpty()) {
			code.add(new CodeSnippet("io.space();"));
			for (Attribute attribute : attrSet) {
				addDomainImport(attribute);
				code.add(attribute.getDomain().getWriteMethod(
						attribute.getName(), "io"));
			}
		}
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}
}
