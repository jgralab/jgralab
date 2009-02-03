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

import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EdgeCodeGenerator extends AttributedElementCodeGenerator {

	public EdgeCodeGenerator(EdgeClass edgeClass, String schemaPackageName,
			String implementationName) {
		super(edgeClass, schemaPackageName, implementationName);
		if (edgeClass instanceof CompositionClass) {
			rootBlock.setVariable("graphElementClass", "Composition");
		} else if (edgeClass instanceof AggregationClass) {
			rootBlock.setVariable("graphElementClass", "Aggregation");
		} else {
			rootBlock.setVariable("graphElementClass", "Edge");
		}
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		CodeList code = new CodeList();
		EdgeClass ec = (EdgeClass) aec;
		code.setVariable("fromVertexClass", ec.getFrom().getQualifiedName());
		code.setVariable("toVertexClass", ec.getTo().getQualifiedName());
		code.setVariable("fromRoleName", ec.getFromRolename());
		code.setVariable("toRoleName", ec.getToRolename());
		code.setVariable("ecName", ec.getQualifiedName());
		CodeSnippet snippet = new CodeSnippet();
		snippet.add("/**");
		snippet.add(" * FromVertexClass: #fromVertexClass#");
		snippet.add(" * FromRoleName : #fromRoleName#");
		snippet.add(" * ToVertexClass: #toVertexClass#");
		snippet.add(" * ToRoleName : #toRoleName#");
		snippet.add(" */");
		code.addNoIndent(snippet);
		code.addNoIndent(super.createHeader(createClass));
		// adds the composition interface, since composition might be no
		// "direct" superclass
		if (aec instanceof CompositionClass) {
			interfaces.add("Composition");
		} else if (aec instanceof AggregationClass) {
			interfaces.add("Aggregation");
		}
		return code;
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = (CodeList) super.createBody(createClass);
		if (createClass) {
			rootBlock.setVariable("baseClassName", "EdgeImpl");
			addImports("#jgImplPackage#.#baseClassName#");
		}
		code.add(createNextEdgeInGraphMethods(createClass));
		code.add(createNextEdgeAtVertexMethods(createClass));
		return code;
	}

	@Override
	protected CodeBlock createGetM1ClassMethod() {
		// TODO Auto-generated method stub
		return super.createGetM1ClassMethod();
	}

	@Override
	protected CodeBlock createSpecialConstructorCode() {
		addImports("#schemaImplPackage#.Reversed#simpleClassName#Impl");
		return new CodeSnippet(
				"reversedEdge = new Reversed#simpleClassName#Impl(this, g);");
	}

	private CodeBlock createNextEdgeInGraphMethods(boolean createClass) {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		for (AttributedElementClass ec : superClasses) {
			if (ec.isInternal()) {
				continue;
			}
			EdgeClass ecl = (EdgeClass) ec;
			code.addNoIndent(createNextEdgeInGraphMethod(ecl, false,
					createClass));
			if (CodeGenerator.CREATE_METHODS_WITH_TYPEFLAG) {
				if (!ecl.isAbstract()) {
					code.addNoIndent(createNextEdgeInGraphMethod(ecl, true,
							createClass));
				}
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeInGraphMethod(EdgeClass ec,
			boolean withTypeFlag, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("ecQualifiedName", schemaRootPackageName + "."
				+ ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"
				: ""));
		code
				.setVariable("actualParams", (withTypeFlag ? ", noSubClasses"
						: ""));

		if (!createClass) {
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
		} else {
			code
					.add(
							"public #ecQualifiedName# getNext#ecCamelName#InGraph(#formalParams#) {",
							"\treturn (#ecQualifiedName#)getNextEdgeOfClassInGraph(#ecQualifiedName#.class#actualParams#);",
							"}");
		}
		return code;
	}

	private CodeBlock createNextEdgeAtVertexMethods(boolean createClass) {
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
			code.addNoIndent(createNextEdgeAtVertexMethod(ecl, false, false,
					createClass));
			code.addNoIndent(createNextEdgeAtVertexMethod(ecl, true, false,
					createClass));
			if (CodeGenerator.CREATE_METHODS_WITH_TYPEFLAG) {
				if (!ecl.isAbstract()) {
					code.addNoIndent(createNextEdgeAtVertexMethod(ecl, false,
							true, createClass));
					code.addNoIndent(createNextEdgeAtVertexMethod(ecl, true,
							true, createClass));
				}
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeAtVertexMethod(EdgeClass ec,
			boolean withOrientation, boolean withTypeFlag, boolean createClass) {

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
		if (!createClass) {
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
		} else {
			code
					.add(
							"public #ecQualifiedName# getNext#ecCamelName#(#formalParams#) {",
							"\treturn (#ecQualifiedName#)getNextEdgeOfClass(#ecQualifiedName#.class#actualParams#);",
							"}");

		}

		return code;
	}

}
