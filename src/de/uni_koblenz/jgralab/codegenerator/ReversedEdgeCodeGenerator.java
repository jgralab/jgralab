/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class ReversedEdgeCodeGenerator extends AttributedElementCodeGenerator {

	public ReversedEdgeCodeGenerator(EdgeClass edgeClass,
			String schemaPackageName, String implementationName) {
		super(edgeClass, schemaPackageName, implementationName);
		rootBlock.setVariable("graphElementClass", "ReversedEdge");
		rootBlock.setVariable("isImplementationClassOnly", "true");
		rootBlock.setVariable("className", "Reversed"
				+ edgeClass.getSimpleName());
		rootBlock.setVariable("simpleClassName", "Reversed"
				+ edgeClass.getSimpleName());
		rootBlock.setVariable("simpleImplClassName", "Reversed"
				+ edgeClass.getSimpleName() + "Impl");
		rootBlock.setVariable("normalQualifiedClassName", schemaRootPackageName
				+ "." + edgeClass.getQualifiedName());
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		if (createClass) {
			rootBlock.setVariable("baseClassName", "ReversedEdgeImpl");
			addImports("#jgImplPackage#.#baseClassName#");
		}
		CodeList code = (CodeList) super.createBody(createClass);
		code.add(createNextEdgeInGraphMethods());
		code.add(createNextEdgeAtVertexMethods());
		return code;
	}

	@Override
	protected CodeBlock createConstructor() {
		addImports("#jgImplPackage#.EdgeImpl", "#jgPackage#.Graph");
		return new CodeSnippet(true, "#className#Impl(EdgeImpl e, Graph g) {",
				"\tsuper(e, g);", "}");
	}

	@Override
	protected CodeBlock createGetter(Attribute a, boolean body) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("name", a.getName());
		code.setVariable("cName", camelCase(a.getName()));
		code.setVariable("type", a.getDomain()
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));
		code.setVariable("isOrGet", a.getDomain().getJavaClassName(
				schemaRootPackageName).equals("Boolean") ? "is" : "get");

		if (body) {
			code
					.add(
							"public #type# #isOrGet##cName#() {",
							"\treturn ((#normalQualifiedClassName#)normalEdge).#isOrGet##cName#();",
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
				.getJavaAttributeImplementationTypeName(schemaRootPackageName));

		if (body) {
			code
					.add(
							"public void set#cName#(#type# #name#) {",
							"\t((#normalQualifiedClassName#)normalEdge).set#cName#(#name#);",
							"}");
		} else {
			code.add("public void set#cName#(#type# #name#);");
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
			if (CodeGenerator.CREATE_METHODS_WITH_TYPEFLAG) {
				if (!ecl.isAbstract()) {
					code.addNoIndent(createNextEdgeInGraphMethod(ecl, true));
				}
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeInGraphMethod(EdgeClass ec,
			boolean withTypeFlag) {
		CodeSnippet code = new CodeSnippet(
				true,
				"public #ecName# getNext#ecCamelName#InGraph(#formalParams#) {",
				"\treturn ((#ecName#)normalEdge).getNext#ecCamelName#InGraph(#actualParams#);",
				"}");

		code.setVariable("ecName", schemaRootPackageName + "."
				+ ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
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
		code.setVariable("ecName", schemaRootPackageName + "."
				+ ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
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

}
