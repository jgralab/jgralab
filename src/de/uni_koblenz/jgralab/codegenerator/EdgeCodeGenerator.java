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

import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EdgeCodeGenerator extends AttributedElementCodeGenerator {

	public EdgeCodeGenerator(EdgeClass edgeClass, String schemaPackageName,
			String implementationName, CodeGeneratorConfiguration config) {
		super(edgeClass, schemaPackageName, implementationName, config);
		rootBlock.setVariable("graphElementClass", "Edge");
	}

	@Override
	protected CodeBlock createHeader() {
		CodeList code = new CodeList();
		EdgeClass ec = (EdgeClass) aec;
		code.setVariable("fromVertexClass", ec.getFrom().getVertexClass()
				.getQualifiedName());
		code.setVariable("toVertexClass", ec.getTo().getVertexClass()
				.getQualifiedName());
		code.setVariable("fromRoleName", ec.getFrom().getRolename());
		code.setVariable("toRoleName", ec.getTo().getRolename());
		code.setVariable("ecName", ec.getQualifiedName());
		CodeSnippet snippet = new CodeSnippet();
		snippet.add("/**");
		snippet.add(" * FromVertexClass: #fromVertexClass#");
		snippet.add(" * FromRoleName : #fromRoleName#");
		snippet.add(" * ToVertexClass: #toVertexClass#");
		snippet.add(" * ToRoleName : #toRoleName#");
		snippet.add(" */");
		code.addNoIndent(snippet);
		code.addNoIndent(super.createHeader());
		return code;
	}

	@Override
	protected CodeBlock createConstructor() {
		CodeList code = new CodeList();
		addImports("#jgPackage#.Vertex");
		code
				.addNoIndent(new CodeSnippet(
						true,
						"public #simpleClassName#Impl(int id, #jgPackage#.Graph g, Vertex alpha, Vertex omega) {",
						"\tsuper(id, g, alpha, omega);",
						"\tinitializeAttributesWithDefaultValues();"));
		code.add(createSpecialConstructorCode());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = (CodeList) super.createBody();
		if (currentCycle.isStdOrTransImpl()) {
			rootBlock.setVariable("baseClassName", "EdgeImpl");
			if (currentCycle.isStdImpl()) {
				addImports("#jgImplStdPackage#.#baseClassName#");
			}
			if (currentCycle.isTransImpl()) {
				addImports("#jgImplTransPackage#.#baseClassName#");
			}
		}
		if (config.hasTypeSpecificMethodsSupport()
				&& !currentCycle.isClassOnly()) {
			code.add(createNextEdgeInGraphMethods());
			code.add(createNextEdgeAtVertexMethods());
		}
		if (currentCycle.isStdOrTransImpl()) {
			code.add(createGetSemanticsMethod());
			code.add(createGetAlphaSemanticsMethod());
			code.add(createGetOmegaSemanticsMethod());
			code.add(createReversedEdgeMethod());
		}
		// code.add(createValidRolesMethod());
		return code;
	}

	/**
	 * 
	 * @return
	 */
	private CodeBlock createReversedEdgeMethod() {
		CodeSnippet code = new CodeSnippet(true,
				"protected #jgImplPackage#.ReversedEdgeBaseImpl createReversedEdge() {");
		if (currentCycle.isStdImpl()) {
			addImports("#schemaImplStdPackage#.Reversed#simpleClassName#Impl");
		}
		if (currentCycle.isTransImpl()) {
			addImports("#schemaImplTransPackage#.Reversed#simpleClassName#Impl");
		}
		code.add("\treturn new Reversed#simpleClassName#Impl(this, graph);");
		code.add("}");
		return code;
	}

	private CodeBlock createNextEdgeInGraphMethods() {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		if (config.hasTypeSpecificMethodsSupport()) {
			for (AttributedElementClass ec : superClasses) {
				if (ec.isInternal()) {
					continue;
				}
				EdgeClass ecl = (EdgeClass) ec;
				code.addNoIndent(createNextEdgeInGraphMethod(ecl, false));
				if (config.hasMethodsForSubclassesSupport()) {
					if (!ecl.isAbstract()) {
						code
								.addNoIndent(createNextEdgeInGraphMethod(ecl,
										true));
					}
				}
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeInGraphMethod(EdgeClass ec,
			boolean withTypeFlag) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("ecQualifiedName", schemaRootPackageName + "."
				+ ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"
				: ""));
		code
				.setVariable("actualParams", (withTypeFlag ? ", noSubClasses"
						: ""));

		if (currentCycle.isAbstract()) {
			code
					.add("/**",
							" * @return the next #ecQualifiedName# edge in the global edge sequence");
			if (withTypeFlag) {
				code
						.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #ecQualifiedName# are accepted");
			}
			code
					.add(" */",
							"public #ecQualifiedName# getNext#ecCamelName#InGraph(#formalParams#);");
		}
		if (currentCycle.isStdOrTransImpl()) {
			code
					.add(
							"public #ecQualifiedName# getNext#ecCamelName#InGraph(#formalParams#) {",
							"\treturn (#ecQualifiedName#)getNextEdgeOfClassInGraph(#ecQualifiedName#.class#actualParams#);",
							"}");
		}
		return code;
	}

	private CodeBlock createNextEdgeAtVertexMethods() {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		if (config.hasTypeSpecificMethodsSupport()) {
			for (AttributedElementClass ec : superClasses) {
				if (ec.isInternal()) {
					continue;
				}
				addImports("#jgPackage#.EdgeDirection");
				EdgeClass ecl = (EdgeClass) ec;
				code
						.addNoIndent(createNextEdgeAtVertexMethod(ecl, false,
								false));
				code
						.addNoIndent(createNextEdgeAtVertexMethod(ecl, true,
								false));
				if (config.hasMethodsForSubclassesSupport()) {
					if (!ecl.isAbstract()) {
						code.addNoIndent(createNextEdgeAtVertexMethod(ecl,
								false, true));
						code.addNoIndent(createNextEdgeAtVertexMethod(ecl,
								true, true));
					}
				}
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeAtVertexMethod(EdgeClass ec,
			boolean withOrientation, boolean withTypeFlag) {

		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("ecQualifiedName", schemaRootPackageName + "."
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
		if (currentCycle.isAbstract()) {
			code
					.add("/**",
							" * @return the next edge of class #ecQualifiedName# at the \"this\" vertex");

			if (withOrientation) {
				code.add(" * @param orientation the orientation of the edge");
			}
			if (withTypeFlag) {
				code
						.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #ecQualifiedName# are accepted");
			}
			code
					.add(" */",
							"public #ecQualifiedName# getNext#ecCamelName#(#formalParams#);");
		}
		if (currentCycle.isStdOrTransImpl()) {
			code
					.add(
							"public #ecQualifiedName# getNext#ecCamelName#(#formalParams#) {",
							"\treturn (#ecQualifiedName#)getNextEdgeOfClass(#ecQualifiedName#.class#actualParams#);",
							"}");

		}

		return code;
	}

	private CodeBlock createGetSemanticsMethod() {
		CodeSnippet code = new CodeSnippet(true);
		EdgeClass ec = (EdgeClass) aec;
		String val = "NONE";
		if ((ec.getTo().getAggregationKind() == AggregationKind.COMPOSITE)
				|| (ec.getFrom().getAggregationKind() == AggregationKind.COMPOSITE)) {
			val = "SHARED";
		} else if ((ec.getTo().getAggregationKind() == AggregationKind.SHARED)
				|| (ec.getFrom().getAggregationKind() == AggregationKind.SHARED)) {
			val = "COMPOSITE";
		}
		code.setVariable("semantics", val);
		code
				.add(
						"public de.uni_koblenz.jgralab.schema.AggregationKind getSemantics() {",
						"\treturn de.uni_koblenz.jgralab.schema.AggregationKind.#semantics#;",
						"}");
		return code;
	}

	private CodeBlock createGetAlphaSemanticsMethod() {
		CodeSnippet code = new CodeSnippet(true);
		EdgeClass ec = (EdgeClass) aec;
		code.setVariable("semantics", ec.getFrom().getAggregationKind()
				.toString());
		code
				.add(
						"@Override",
						"public de.uni_koblenz.jgralab.schema.AggregationKind getAlphaSemantics() {",
						"\treturn de.uni_koblenz.jgralab.schema.AggregationKind.#semantics#;",
						"}");
		return code;
	}

	private CodeBlock createGetOmegaSemanticsMethod() {
		CodeSnippet code = new CodeSnippet(true);
		EdgeClass ec = (EdgeClass) aec;
		code.setVariable("semantics", ec.getTo().getAggregationKind()
				.toString());
		code
				.add(
						"@Override",
						"public de.uni_koblenz.jgralab.schema.AggregationKind getOmegaSemantics() {",
						"\treturn de.uni_koblenz.jgralab.schema.AggregationKind.#semantics#;",
						"}");
		return code;
	}

}
