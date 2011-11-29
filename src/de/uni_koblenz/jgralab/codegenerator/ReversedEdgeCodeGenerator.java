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
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ReversedEdgeCodeGenerator extends AttributedElementCodeGenerator {

	public ReversedEdgeCodeGenerator(EdgeClass edgeClass,
			String schemaPackageName, CodeGeneratorConfiguration config) {
		super(edgeClass, schemaPackageName, config);
		rootBlock.setVariable("graphElementClass", "ReversedEdge");
		rootBlock.setVariable("isImplementationClassOnly", "true");
		rootBlock.setVariable("className",
				"Reversed" + edgeClass.getSimpleName());
		rootBlock.setVariable("simpleClassName",
				"Reversed" + edgeClass.getSimpleName());
		rootBlock.setVariable("simpleImplClassName",
				"Reversed" + edgeClass.getSimpleName() + "Impl");
		rootBlock.setVariable("normalQualifiedClassName", schemaRootPackageName
				+ "." + edgeClass.getQualifiedName());
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = (CodeList) super.createBody();
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			rootBlock.setVariable("baseClassName", "ReversedEdgeImpl");

			if (currentCycle.isStdImpl()) {
				addImports("#jgImplStdPackage#.#baseClassName#");
			} else if (currentCycle.isTransImpl()) {
				addImports("#jgImplTransPackage#.#baseClassName#");
			} else if (currentCycle.isDbImpl()) {
				addImports("#jgImplDbPackage#.#baseClassName#");
			}

			if (config.hasTypeSpecificMethodsSupport()) {
				code.add(createNextEdgeMethods());
				code.add(createNextIncidenceMethods());
			}
			code.add(createGetAlphaOmegaOverrides());
		}
		return code;
	}

	@Override
	protected CodeBlock createConstructor() {
		// TODO Introduce constants for jgImplStdPackage etc. (refactor)
		if (currentCycle.isStdImpl()) {
			addImports("#jgImplStdPackage#.EdgeImpl", "#jgPackage#.Graph");
		}
		if (currentCycle.isTransImpl()) {
			addImports("#jgImplTransPackage#.EdgeImpl", "#jgPackage#.Graph");
		}
		if (currentCycle.isDbImpl()) {
			addImports("#jgImplDbPackage#.EdgeImpl", "#jgPackage#.Graph");
		}

		return new CodeSnippet(true, "#className#Impl(EdgeImpl e, Graph g) {",
				"\tsuper(e, g);", "}");
	}

	private CodeBlock createGetAlphaOmegaOverrides() {
		CodeSnippet b = new CodeSnippet();
		EdgeClass ec = (EdgeClass) aec;
		VertexClass from = ec.getFrom().getVertexClass();
		VertexClass to = ec.getTo().getVertexClass();
		b.setVariable("fromVertexClass", from.getSimpleName());
		b.setVariable("toVertexClass", to.getSimpleName());
		addImports(schemaRootPackageName + "." + from.getQualifiedName());
		addImports(schemaRootPackageName + "." + to.getQualifiedName());
		if (!currentCycle.isAbstract()) {
			b.add("public #fromVertexClass# getAlpha() {");
			b.add("\treturn (#fromVertexClass#) super.getAlpha();");
			b.add("}");
			b.add("public #toVertexClass# getOmega() {");
			b.add("\treturn (#toVertexClass#) super.getOmega();");
			b.add("}");
		}
		return b;
	}

	@Override
	protected CodeBlock createGetter(Attribute a) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", a.getName());
		code.setVariable("type", a.getDomain()
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));
		code.setVariable("isOrGet", a.getDomain().isBoolean() ? "is" : "get");

		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.add(
					"public #type# #isOrGet#_#name#() {",
					"\treturn ((#normalQualifiedClassName#)normalEdge).#isOrGet#_#name#();",
					"}");
		}
		if (currentCycle.isAbstract()) {
			code.add("public #type# #isOrGet#_#name#();");
		}
		return code;
	}

	@Override
	protected CodeBlock createSetter(Attribute a) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", a.getName());
		code.setVariable("type", a.getDomain()
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));

		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.add(
					"public void set_#name#(#type# _#name#) {",
					"\t((#normalQualifiedClassName#)normalEdge).set_#name#(_#name#);",
					"}");
		}
		if (currentCycle.isAbstract()) {
			code.add("public void set_#name#(#type# _#name#);");
		}
		return code;
	}

	@Override
	protected CodeBlock createGenericGetter(Set<Attribute> attrSet) {
		return null;
	}

	@Override
	protected CodeBlock createGenericSetter(Set<Attribute> attrSet) {
		return null;
	}

	private CodeBlock createNextEdgeMethods() {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		for (AttributedElementClass ec : superClasses) {
			if (ec.isInternal()) {
				continue;
			}
			EdgeClass ecl = (EdgeClass) ec;
			code.addNoIndent(createNextEdgeMethod(ecl));
		}
		return code;
	}

	private CodeBlock createNextEdgeMethod(EdgeClass ec) {
		CodeSnippet code = new CodeSnippet(
				true,
				"public #ecName# getNext#ecCamelName#InGraph(#formalParams#) {",
				"\treturn ((#ecName#)normalEdge).getNext#ecCamelName#InGraph(#actualParams#);",
				"}");

		code.setVariable("ecName",
				schemaRootPackageName + "." + ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
		code.setVariable("formalParams", "");
		code.setVariable("actualParams", "");
		return code;
	}

	private CodeBlock createNextIncidenceMethods() {
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
			code.addNoIndent(createNextIncidenceMethod(ecl, false));
			code.addNoIndent(createNextIncidenceMethod(ecl, true));
		}
		return code;
	}

	private CodeBlock createNextIncidenceMethod(EdgeClass ec,
			boolean withOrientation) {

		CodeSnippet code = new CodeSnippet(
				true,
				"public #ecName# getNext#ecCamelName#Incidence(#formalParams#) {",
				"\treturn (#ecName#)getNextIncidence(#ecName#.class#actualParams#);",
				"}");
		code.setVariable("ecName",
				schemaRootPackageName + "." + ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
		code.setVariable("formalParams",
				(withOrientation ? "EdgeDirection orientation" : ""));
		code.setVariable("actualParams", (withOrientation ? ", orientation"
				: ""));
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
	protected CodeBlock createGetSchemaClassMethod() {
		return null;
	}

	@Override
	protected CodeBlock createReadAttributesFromStringMethod(
			Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code.addNoIndent(new CodeSnippet(
				true,
				"public void readAttributeValueFromString(String attributeName, String value) throws GraphIOException {"));
		code.add(new CodeSnippet(
				"throw new GraphIOException(\"Can not call readAttributeValuesFromString for reversed Edges.\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createWriteAttributeToStringMethod(
			Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code.addNoIndent(new CodeSnippet(
				true,
				"public String writeAttributeValueToString(String _attributeName) throws IOException, GraphIOException {"));
		code.add(new CodeSnippet(
				"throw new GraphIOException(\"Can not call writeAttributeValueToString for reversed Edges.\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createReadAttributesMethod(SortedSet<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException");
		code.addNoIndent(new CodeSnippet(true,
				"public void readAttributeValues(GraphIO io) throws GraphIOException {"));
		code.add(new CodeSnippet(
				"throw new GraphIOException(\"Can not call readAttributeValues for reversed Edges.\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createWriteAttributesMethod(Set<Attribute> attrSet) {
		CodeList code = new CodeList();
		addImports("#jgPackage#.GraphIO", "#jgPackage#.GraphIOException",
				"java.io.IOException");
		code.addNoIndent(new CodeSnippet(
				true,
				"public void writeAttributeValues(GraphIO io) throws GraphIOException, IOException {"));
		code.add(new CodeSnippet(
				"throw new GraphIOException(\"Can not call writeAttributeValues for reversed Edges.\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createGetVersionedAttributesMethod(
			SortedSet<Attribute> attributeList) {
		if (currentCycle.isTransImpl()) {
			// delegate to attributes()-method in corresponding normalEdge
			CodeSnippet code = new CodeSnippet();
			code.add("protected java.util.Set<de.uni_koblenz.jgralab.trans.VersionedDataObject<?>> attributes() {");
			code.add("\treturn ((EdgeImpl) normalEdge).attributes();");
			code.add("}");
			return code;
		}
		return null;
	}

}
