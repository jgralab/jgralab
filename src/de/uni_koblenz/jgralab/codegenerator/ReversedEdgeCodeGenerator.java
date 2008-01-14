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
import java.util.TreeSet;

import de.uni_koblenz.jgralab.AggregationClass;
import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.CompositionClass;
import de.uni_koblenz.jgralab.EdgeClass;

public class ReversedEdgeCodeGenerator extends AttributedElementCodeGenerator {

	public ReversedEdgeCodeGenerator(EdgeClass edgeClass,
			String schemaPackageName, String implementationName) {
		super(edgeClass, schemaPackageName, implementationName);
		rootBlock.setVariable("graphElementClass", "ReversedEdge");
		rootBlock.setVariable("isImplementationClassOnly", "true");
		rootBlock.setVariable("className", "Reversed" + edgeClass.getName());
		rootBlock.setVariable("implClassName", "Reversed" + edgeClass.getName()
				+ "Impl");
		rootBlock.setVariable("normalClassName", edgeClass.getName());
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		if (aec instanceof CompositionClass) {
			addImports("#jgPackage#.Composition");
		} else if (aec instanceof AggregationClass) {
			addImports("#jgPackage#.Aggregation");
		} else {
			addImports("#jgPackage#.Edge");
		}
		return super.createHeader(createClass);
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		if (createClass) {
			if (aec.getAllSuperClasses().contains(aec.getSchema().getAttributedElementClass("Composition")))
				rootBlock.setVariable("baseClassName", "ReversedCompositionImpl");		
			else if (aec.getAllSuperClasses().contains(aec.getSchema().getAttributedElementClass("Aggregation")))
					rootBlock.setVariable("baseClassName", "ReversedAggregationImpl");
			else rootBlock.setVariable("baseClassName", "ReversedEdgeImpl");
			addImports("#jgImplPackage#.#jgImplementation#.#baseClassName#");
		}
		CodeList code = (CodeList) super.createBody(createClass);
		code.add(createNextEdgeInGraphMethods());
		code.add(createNextEdgeAtVertexMethods());
		return code;
	}

	@Override
	protected CodeBlock createConstructor() {
		addImports("#jgImplPackage#.#jgImplementation#.EdgeImpl",
				"#jgPackage#.Graph");
		return new CodeSnippet(true, "#className#Impl(EdgeImpl e, Graph g) {",
				"\tsuper(e, g);", "}");
	}

	@Override
	protected CodeBlock createGetter(Attribute a, boolean body) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", a.getName());
		code.setVariable("cName", camelCase(a.getName()));
		code.setVariable("type", a.getDomain()
				.getJavaAttributeImplementationTypeName());
		code.setVariable("isOrGet", a.getDomain().getJavaClassName().equals(
				"Boolean") ? "is" : "get");

		if (body) {
			addDomainImport(a);
			code
					.add(
							"public #type# #isOrGet##cName#() {",
							"\treturn ((#normalClassName#)normalEdge).#isOrGet##cName#();",
							"}");
		} else {
			code.add("public #type# #isOrGet##cName#();");
		}
		return code;
	}

	@Override
	protected CodeBlock createSetter(Attribute a, boolean body) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", a.getName());
		code.setVariable("cName", camelCase(a.getName()));
		code.setVariable("type", a.getDomain()
				.getJavaAttributeImplementationTypeName());

		if (body) {
			addDomainImport(a);
			code.add("public void set#cName#(#type# #name#) {",
					"\t((#normalClassName#)normalEdge).set#cName#(#name#);",
					"}");
		} else {
			code.add("public void set#cName#(#type# #name#);");
		}
		return code;
	}

	@Override
	protected CodeBlock createGenericGetter(Set<Attribute> attrSet) {
		return new CodeSnippet(
				true,
				"public Object getAttribute(String attributeName) throws NoSuchFieldException {",
				"\treturn ((#normalClassName#)normalEdge).getAttribute(attributeName);",
				"}");
	}
	
	@Override
	protected CodeBlock createGenericSetter(Set<Attribute> attrSet) {
		return new CodeSnippet(
				true,
				"public void setAttribute(String attributeName, Object data) throws NoSuchFieldException {",
				"\t((#normalClassName#)normalEdge).setAttribute(attributeName, data);",
				"}");
	}

	private CodeBlock createNextEdgeInGraphMethods() {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		for (AttributedElementClass ec : superClasses) {
			if (ec.isInternal()) {
				continue;
			}
			EdgeClass ecl = (EdgeClass) ec;
			code.addNoIndent(createNextEdgeInGraphMethod(ecl, false));
			if (!ecl.isAbstract()) {
				code.addNoIndent(createNextEdgeInGraphMethod(ecl, true));
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeInGraphMethod(EdgeClass ec,	boolean withTypeFlag) {
		CodeSnippet code = new CodeSnippet(
				true,
				"public #ecName# getNext#ecCamelName#InGraph(#formalParams#) {",
				"\treturn ((#ecName#)normalEdge).getNext#ecCamelName#InGraph(#actualParams#);",
				"}");

		code.setVariable("ecName", ec.getName());
		code.setVariable("ecCamelName", camelCase(ec.getName()));
		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"
				: ""));
		code.setVariable("actualParams", (withTypeFlag ? "noSubClasses" : ""));
		return code;
	}

	private CodeBlock createNextEdgeAtVertexMethods() {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		for (AttributedElementClass ec : superClasses) {
			if (ec.isInternal()) {
				continue;
			}
			addImports("#jgPackage#.EdgeDirection");
			EdgeClass ecl = (EdgeClass) ec;
			code.addNoIndent(createNextEdgeAtVertexMethod(ecl, false, false));
			code.addNoIndent(createNextEdgeAtVertexMethod(ecl, true, false));
			if (!ecl.isAbstract()) {
				code
						.addNoIndent(createNextEdgeAtVertexMethod(ecl, false,
								true));
				code.addNoIndent(createNextEdgeAtVertexMethod(ecl, true, true));
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeAtVertexMethod(EdgeClass ec,
			boolean withOrientation, boolean withTypeFlag) {

		CodeSnippet code = new CodeSnippet(
				true,
				"public #ecName# getNext#ecCamelName#(#formalParams#) {",
				"\treturn (#ecName#)getNextEdgeOfClass(#ecName#.class#actualParams#);",
				"}");
		code.setVariable("ecName", ec.getName());
		code.setVariable("ecCamelName", camelCase(ec.getName()));
		code.setVariable("formalParams",
				(withOrientation ? "EdgeDirection orientation" : "")
						+ (withOrientation && withTypeFlag ? ", " : "")
						+ (withTypeFlag ? "boolean noSubClasses" : ""));
		code.setVariable("actualParams",
				(withOrientation || withTypeFlag ? ", " : "")
						+ (withOrientation ? "orientation" : "")
						+ (withOrientation && withTypeFlag ? ", " : "")
						+ (withTypeFlag ? "noSubClasses" : ""));
		return code;
	}

	@Override
	protected CodeBlock createStaticImplementationClassField() {
		return null;
	}

	@Override
	protected CodeBlock createFields(Set<Attribute> attrSet) {
		return null;
	}

	@Override
	protected CodeBlock createGetM1ClassMethod() {
		return null;
	}

	@Override
	protected CodeBlock createReadAttributesMethod(Set<Attribute> attrSet) {
		return super.createReadAttributesMethod(null);
	}

	@Override
	protected CodeBlock createWriteAttributesMethod(Set<Attribute> attrSet) {
		return super.createWriteAttributesMethod(null);
	}

	// protected String createBody(boolean createClass) {
	// StringBuffer code = new StringBuffer();
	// if (createClass) {
	// this.addImport(jgralabSpecialImplPackageName + ".EdgeImpl");
	// this.addImport(jgralabSpecialImplPackageName + ".ReversedEdgeImpl");
	// code.append(defaultConstructor());
	// //code.append(getM1ClassMethod());
	// } else {
	// code.append(implementationClassField(normalEdgeName));
	// }
	// // code.append(getAlphaAndOmega(createClass));
	// // code.append(getThisAndThat(createClass));
	// if (createClass) {
	// addImport(jgralabPackageName + ".GraphIO");
	// addImport(jgralabPackageName + ".GraphIOException");
	// addImport("java.io.IOException");
	// code.append(readAttributesMethod());
	// code.append(writeAttributesMethod());
	// }
	// return code.toString();
	// }
	//
	// private String delegations(boolean createBody) {
	// StringBuffer code = new StringBuffer();
	// Iterator<Attribute> it = completeAttributeSet.iterator();
	// String attrName, attrDomain;
	// Attribute attr;
	// while (it.hasNext()) {
	// attr = it.next();
	// attrName = attr.getName();
	// attrDomain = attr.getDomain()
	// .getJavaAttributeImplementationTypeName();
	//
	// // getter
	// code.append(indentation + "public " + attrDomain);
	// if (attr.getDomain().getJavaClassName() == "Boolean")
	// code.append(" is");
	// else
	// code.append(" get");
	// code.append(camelCase(attrName) + "()");
	// if (createBody) {
	// code.append(" {\n");
	// code.append(indentation2 + "return ((" + normalEdgeName +
	// ")normalEdge).");
	// if (attr.getDomain().getJavaClassName() == "Boolean")
	// code.append("is");
	// else
	// code.append("get");
	// code.append(camelCase(attrName) + "();\n");
	// code.append(indentation + "}\n\n");
	// } else {
	// code.append(";\n\n");
	// }
	//
	// // setter
	// code.append(indentation + "public void set" + camelCase(attrName) + "("
	// + attrDomain + " " + attrName + ")");
	// if (createBody) {
	// code.append(" {\n");
	// code.append(indentation2 + "((" + normalEdgeName + ")normalEdge).set"
	// + camelCase(attrName) + "(" + attrName + ");\n");
	// code.append(indentation + "}\n\n");
	// } else {
	// code.append(";\n\n");
	// }
	//
	// }
	// return code.toString();
	// }
	//
	// /**
	// * example output: public Street getNextStreetInGraph() { return
	// (Street)getEdge(); }
	// *
	// * @param body
	// * if set to TRUE, a {...}-body is created
	// * @return generated code string which is required by all incidences
	// */
	// private String nextEdgeInGraphMethods(boolean body) {
	// StringBuffer code = new StringBuffer();
	// String edgeName;
	// Iterator<AttributedElementClass> it = completeSuperClasses.iterator();
	// while (it.hasNext()) {
	// AttributedElementClass superClass = it.next();
	// if (superClass.isInternal())
	// continue;
	// edgeName = superClass.getName();
	// for (int i = 0; i < 2; i++) {
	// if (!body) {
	// code.append(indentation + "/**\n");
	// code.append(indentation + " * @return the next " + edgeName + " in the
	// graphs global edge sequence\n");
	// code.append(indentation + " */\n");
	// }
	// code.append(indentation + "public " + edgeName + " getNext");
	// code.append(camelCase(edgeName) + "InGraph(");
	// if (i == 1)
	// code.append("boolean explicitType");
	// code.append(")");
	// if (body) {
	// code.append(" {\n");
	// code.append(indentation + indentation + "return (");
	// code.append(edgeName);
	// code.append(")getNextEdgeOfClassInGraph(");
	// code.append(edgeName + ".class");
	// if (i == 1)
	// code.append(", explicitType");
	// code.append(");\n");
	// code.append(indentation + "}\n\n");
	// } else {
	// code.append(";\n");
	// }
	// code.append("\n");
	// }
	// }
	// return code.toString();
	// }
	//
	// private String nextEdgeAtVertexMethods(boolean body) {
	// addImport(jgralabPackageName + ".EdgeDirection");
	// StringBuffer code = new StringBuffer();
	// String edgeName, theClass;
	// Iterator<AttributedElementClass> it = completeSuperClasses
	// .iterator();
	// AttributedElementClass aec;
	// while (it.hasNext()) {
	// aec = it.next();
	// if (aec.isInternal())
	// continue;
	// theClass = aec.getName();
	// edgeName = aec.getName();
	// for (int i = 0; i < 4; i++) {
	// if (!body) {
	// code.append(indentation + "/**\n");
	// code.append(indentation + " * @return the next " + edgeName + " at this
	// vertex\n");
	// if ((i == 0) | (i == 1))
	// code.append(indentation + " * @param orientation the orientation of the
	// next edge at the vertex, may be <code>EdgeDirection.IN</code> or
	// <code>EdgeDirection.OUT</code>\n");
	// if ((i == 1) | (i == 3))
	// code.append(indentation + " * @param explicitType if set to
	// <code>true</code>, only edges which have explicit the given type,
	// subclasses of this type are <b>not</b> accepted\n");
	// code.append(indentation + " */\n");
	// }
	// code.append(indentation + "public " + edgeName + " getNext");
	// code.append(camelCase(edgeName) + "(");
	// if ((i == 0) | (i == 1))
	// code.append("EdgeDirection orientation");
	// if (i == 1)
	// code.append(", ");
	// if ((i == 1) | (i == 3))
	// code.append("boolean explicitType");
	// code.append(")");
	// if (body) {
	// code.append("{\n");
	// code.append(indentation2 + "return (");
	// code.append(edgeName);
	// code.append(")getNextEdgeOf");
	// code.append("Class(");
	// code.append(theClass);
	// code.append(".class");
	// if ((i == 0) | (i == 1))
	// code.append(", orientation");
	// if ((i == 1) | (i == 3))
	// code.append(", explicitType");
	// code.append(");\n");
	// code.append(indentation + "}\n");
	// } else {
	// code.append(";\n");
	// }
	// code.append("\n");
	// }
	// }
	// return code.toString();
	// }
	//
	// /**
	// * @return code string to build the fill method for loading a tg file
	// */
	// protected String readAttributesMethod() {
	// StringBuffer code = new StringBuffer();
	// code.append(indentation + "public void readAttributeValues(GraphIO io)
	// throws GraphIOException {\n");
	// code.append(indentation + "}\n\n");
	// return code.toString();
	// }
	//
	// /**
	// * @return code string to build the getValues method for saving a tg file
	// */
	// protected String writeAttributesMethod() {
	// StringBuffer code = new StringBuffer();
	// code.append(indentation);
	// code.append("public void writeAttributeValues(GraphIO io) throws
	// IOException, GraphIOException {\n");
	// code.append(indentation2);
	// code.append("}\n\n");
	// return code.toString();
	// }
	//	
	//
	// protected String defaultConstructor() {
	// StringBuffer code = new StringBuffer();
	// addImport(jgralabPackageName + ".Graph");
	// code.append(indentation + "/**\n");
	// code.append(indentation + "* The constructor of an Edge has only package
	// visibility, because Edges should be constructed <b>only</b> by the
	// Graph-object they belong to\n");
	// code.append(indentation + "*/\n");
	// code.append(indentation + classToGenerateName);
	// if (separateInterfaceAndImplementation)
	// code.append("Impl");
	// code.append("(EdgeImpl edge, Graph graph) {\n");
	// code.append(indentation2 + "super(edge, graph);\n");
	// code.append(indentation + "}\n\n");
	// return code.toString();
	// }
	//	
	// protected String getM1ClassMethod() {
	// StringBuffer code = new StringBuffer();
	// code.append(indentation);
	// code.append("public Class getM1Class() {\n");
	// code.append(indentation2);
	// code.append("return " + normalEdgeName + ".class;\n");
	// code.append(indentation);
	// code.append("}\n\n");
	// return code.toString();
	// }

}
