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
import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This class is used by the method Schema.commit() to generate the Java-classes
 * that implement the VertexClasses of a graph schema.
 * 
 * @author ist@uni-koblenz.de
 */
public class VertexCodeGenerator extends AttributedElementCodeGenerator {

	private RolenameCodeGenerator rolenameGenerator;

	public VertexCodeGenerator(VertexClass vertexClass,
			String schemaPackageName, String implementationName,
			CodeGeneratorConfiguration config) {
		super(vertexClass, schemaPackageName, implementationName,
				config);
		rootBlock.setVariable("graphElementClass", "Vertex");
		rolenameGenerator = new RolenameCodeGenerator((VertexClass) aec);
	}

	/**
	 * creates the header of the classfile, that is the part
	 * <code>public class VertexClassName extends Vertex {</code>
	 */
	@Override
	protected CodeBlock createHeader() {
		return super.createHeader();
	}

	/**
	 * creates the body of the classfile, that are methods and attributes
	 */
	@Override
	protected CodeBlock createBody() {
		CodeList code = (CodeList) super.createBody();
		if (currentCycle.isStdOrTransImpl()) {
			if (currentCycle.isStdImpl())
				addImports("#jgImplStdPackage#.#baseClassName#");
			else
				addImports("#jgImplTransPackage#.#baseClassName#");
			rootBlock.setVariable("baseClassName", "VertexImpl");
			code.add(createValidEdgeSets((VertexClass) aec));
		}
		if (config.hasTypeSpecificMethodsSupport() && !currentCycle.isClassOnly()) {
			code.add(createNextVertexMethods());
			code.add(createFirstEdgeMethods());
			code.add(rolenameGenerator.createRolenameMethods(currentCycle.isStdOrTransImpl()));
			code.add(createIncidenceIteratorMethods());
		}	
		return code;
	}

	/**
	 * creates the methods <code>getFirstEdgeName()</code>
	 * 
	 * @param createClass
	 *            iff set to true, the method bodies will also be created
	 * @return the CodeBlock that contains the methods
	 */
	private CodeBlock createFirstEdgeMethods() {
		CodeList code = new CodeList();
		for (EdgeClass ec : ((VertexClass) aec).getEdgeClasses()) {
			if (ec.isInternal()) {
				continue;
			}
			addImports("#jgPackage#.EdgeDirection");
			if (config.hasTypeSpecificMethodsSupport()) {
				code.addNoIndent(createFirstEdgeMethod(ec, false, false));
				code.addNoIndent(createFirstEdgeMethod(ec, true, false));

				if (config.hasMethodsForSubclassesSupport()) {
					if (!ec.isAbstract()) {
						code.addNoIndent(createFirstEdgeMethod(ec, false, true));
						code.addNoIndent(createFirstEdgeMethod(ec, true, true));
					}
				}
			}
		}
		return code;
	}

	/**
	 * creates the method <code>getFirstEdgeName()</code> for the given
	 * EdgeClass
	 * 
	 * @param createClass
	 *            iff set to true, the method bodies will also be created
	 * @param withOrientation
	 *            toggles if the EdgeDirection-parameter will be created
	 * @param withTypeFlag
	 *            toggles if the "no subclasses"-parameter will be created
	 * @return the CodeBlock that contains the method
	 */
	private CodeBlock createFirstEdgeMethod(EdgeClass ec,
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
			code.add("/**", " * @return the first edge of class #ecCamelName# at this vertex");

			if (withOrientation) {
				code.add(" * @param orientation the orientation of the edge");
			}
			if (withTypeFlag) {
				code.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #ecName# are accepted");
			}
			code.add(" */",
							"public #ecQualifiedName# getFirst#ecCamelName#(#formalParams#);");
		} 
		if(currentCycle.isStdOrTransImpl()) {
			code.add("public #ecQualifiedName# getFirst#ecCamelName#(#formalParams#) {",
					 "\treturn (#ecQualifiedName#)getFirstEdgeOfClass(#ecQualifiedName#.class#actualParams#);",
					 "}");

		}
		return code;
	}

	/**
	 * creates the <code>getNextVertexClassName()</code> methods
	 * 
	 * @param createClass
	 *            iff set to true, also the method bodies will be created
	 * @return the CodeBlock that contains the methods
	 */
	private CodeBlock createNextVertexMethods() {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		if (config.hasTypeSpecificMethodsSupport())
			for (AttributedElementClass ec : superClasses) {
				if (ec.isInternal()) {
					continue;
				}
				VertexClass vc = (VertexClass) ec;
				code.addNoIndent(createNextVertexMethod(vc, false));
				if (config.hasMethodsForSubclassesSupport()) {
					if (!vc.isAbstract()) {
						code.addNoIndent(createNextVertexMethod(vc, true));
					}
				}
			}
		return code;
	}

	/**
	 * creates the <code>getNextVertexClassName()</code> method for the given
	 * VertexClass
	 * 
	 * @param createClass
	 *            iff set to true, the method bodies will also be created
	 * @param withTypeFlag
	 *            toggles if the "no subclasses"-parameter will be created
	 * @return the CodeBlock that contains the method
	 */
	private CodeBlock createNextVertexMethod(VertexClass vc,
			boolean withTypeFlag) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("vcQualifiedName", schemaRootPackageName + "."
				+ vc.getQualifiedName());
		code.setVariable("vcCamelName", camelCase(vc.getUniqueName()));
		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"	: ""));
		code.setVariable("actualParams", (withTypeFlag ? ", noSubClasses": ""));

		if (currentCycle.isAbstract()) {
			code.add("/**",	" * @return the next #vcQualifiedName# vertex in the global vertex sequence");
			if (withTypeFlag) {
				code.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #vcName# are accepted");
			}
			code.add(" */",	"public #vcQualifiedName# getNext#vcCamelName#(#formalParams#);");
		} 
		if(currentCycle.isStdOrTransImpl()) {
			code.add("public #vcQualifiedName# getNext#vcCamelName#(#formalParams#) {",
					 "\treturn (#vcQualifiedName#)getNextVertexOfClass(#vcQualifiedName#.class#actualParams#);",
				 	 "}");
		}
		return code;
	}

	/**
	 * creates the <code>getEdgeNameIncidences</code> methods
	 * 
	 * @param createClass
	 *            if set to true, also the method bodies will be created
	 * @return the CodeBlock that contains the code for the
	 *         getEdgeNameIncidences-methods
	 */
	private CodeBlock createIncidenceIteratorMethods() {
		VertexClass vc = (VertexClass) aec;

		CodeList code = new CodeList();
		
		if (!config.hasTypeSpecificMethodsSupport() || currentCycle.isClassOnly())
			return code;

		Set<EdgeClass> edgeClassSet = null;
		if (currentCycle.isStdOrTransImpl()) {
			edgeClassSet = vc.getEdgeClasses();
		} 
		if(currentCycle.isAbstract()) {
			edgeClassSet = vc.getOwnEdgeClasses();
		}

		for (EdgeClass ec : edgeClassSet) {
			if (ec.isInternal()) {
				continue;
			}

			if (currentCycle.isStdOrTransImpl()) {
				addImports("#jgImplPackage#.IncidenceIterable");
			}

			CodeSnippet s = new CodeSnippet(true);
			code.addNoIndent(s);

			String targetClassName = schemaRootPackageName + "."
					+ ec.getQualifiedName();
			s.setVariable("edgeClassSimpleName", ec.getSimpleName());
			s.setVariable("edgeClassQualifiedName", targetClassName);
			s.setVariable("edgeClassUniqueName", ec.getUniqueName());

			// getFooIncidences()
			if (currentCycle.isAbstract()) {
				s.add("/**");
				s.add(" * Returns an Iterable for all incidence edges of this vertex that are of type #edgeClassSimpleName# or subtypes.");
				s.add(" */");
				s.add("public Iterable<#edgeClassQualifiedName#> get#edgeClassUniqueName#Incidences();");
			} 
			if(currentCycle.isStdOrTransImpl()) {
				s.add("public Iterable<#edgeClassQualifiedName#> get#edgeClassUniqueName#Incidences() {");
				s.add("\treturn new IncidenceIterable<#edgeClassQualifiedName#>(this, #edgeClassQualifiedName#.class);");
				s.add("}");
			}
			s.add("");
			// getFooIncidences(boolean nosubclasses)
			if (config.hasMethodsForSubclassesSupport()) {
				if (currentCycle.isAbstract()) {
					s.add("/**");
					s.add(" * Returns an Iterable for all incidence edges of this vertex that are of type #edgeClassSimpleName#.");
					s.add(" * @param noSubClasses toggles wether subclasses of #edgeClassName# should be excluded");
					s.add(" */");
					s.add("public Iterable<#edgeClassQualifiedName#> get#edgeClassUniqueName#Incidences(boolean noSubClasses);");
				} 
				if(currentCycle.isStdOrTransImpl()) {
					s.add("public Iterable<#edgeClassQualifiedName#> get#edgeClassUniqueName#Incidences(boolean noSubClasses) {");
					s.add("\treturn new IncidenceIterable<#edgeClassQualifiedName#>(this, #edgeClassQualifiedName#.class, noSubClasses);");
					s.add("}\n");
				}
			}
			// getFooIncidences(EdgeDirection direction, boolean nosubclasses)
			if (config.hasMethodsForSubclassesSupport()) {
				if (currentCycle.isAbstract()) {
					s.add("/**");
					s.add(" * Returns an Iterable for all incidence edges of this vertex that are of type #edgeClassSimpleName#.");
					s.add(" * @param direction EdgeDirection.IN or EdgeDirection.OUT, only edges of this direction will be included in the Iterable");
					s.add(" * @param noSubClasses toggles wether subclasses of #edgeClassName# should be excluded");
					s.add(" */");
					s.add("public Iterable<#edgeClassQualifiedName#> get#edgeClassUniqueName#Incidences(EdgeDirection direction, boolean noSubClasses);");
				} 
				if(currentCycle.isStdOrTransImpl()) {
					s.add("public Iterable<#edgeClassQualifiedName#> get#edgeClassUniqueName#Incidences(EdgeDirection direction, boolean noSubClasses) {");
					s.add("\treturn  new IncidenceIterable<#edgeClassQualifiedName#>(this, #edgeClassQualifiedName#.class, direction, noSubClasses);");
					s.add("}");
				}
			}
			s.add("");
			// getFooIncidences(EdgeDirection direction)
			if (currentCycle.isAbstract()) {
				s.add("/**");
				s.add(" * Returns an Iterable for all incidence edges of this vertex that are of type #edgeClassSimpleName#.");
				s.add(" * @param direction EdgeDirection.IN or EdgeDirection.OUT, only edges of this direction will be included in the Iterable");
				s.add(" */");
				s.add("public Iterable<#edgeClassQualifiedName#> get#edgeClassUniqueName#Incidences(EdgeDirection direction);");
			} 
			if(currentCycle.isStdOrTransImpl()) {
				s.add("public Iterable<#edgeClassQualifiedName#> get#edgeClassUniqueName#Incidences(EdgeDirection direction) {");
				s.add("\treturn new IncidenceIterable<#edgeClassQualifiedName#>(this, #edgeClassQualifiedName#.class, direction);");
				s.add("}");
			}
		}
		return code;
	}

	/**
	 * creates the sets of valid in and valid out edges
	 */
	private CodeBlock createValidEdgeSets(VertexClass vc) {
		addImports("java.util.Set");
		addImports("java.util.HashSet");
		addImports("#jgPackage#.Edge");
		CodeList code = new CodeList();
		code.setVariable("vcQualifiedName", schemaRootPackageName + ".impl."
				+ vc.getQualifiedName());
		code.setVariable("vcCamelName", camelCase(vc.getUniqueName()));
		CodeSnippet s = new CodeSnippet(true);
		s.add("/* add all valid from edges */");
		s
				.add("private static Set<java.lang.Class<? extends Edge>> validFromEdges = new HashSet<java.lang.Class<? extends Edge>>();");
		s.add("");
		s.add("/* (non-Javadoc)");
		s.add(" * @see jgralab.Vertex:isValidAlpha()");
		s.add(" */");
		s.add("public boolean isValidAlpha(Edge edge) {");
		s.add("\treturn validFromEdges.contains(edge.getClass());");
		s.add("}");
		s.add("");
		s.add("{");
		code.addNoIndent(s);
		for (EdgeClass ec : vc.getValidFromEdgeClasses()) {
			CodeSnippet line = new CodeSnippet(true);
			line.setVariable("edgeClassQualifiedName", schemaRootPackageName
					+ ".impl." + (currentCycle.isTransImpl() ?  "trans." : "std.") +  ec.getQualifiedName());
			line.add("\tvalidFromEdges.add(#edgeClassQualifiedName#Impl.class);");
			code.addNoIndent(line);
		}
		s = new CodeSnippet(true);
		s.add("}");
		s.add("");
		s.add("/* add all valid to edges */");
		s
				.add("private static Set<java.lang.Class<? extends Edge>> validToEdges = new HashSet<java.lang.Class<? extends Edge>>();");
		s.add("");
		s.add("/* (non-Javadoc)");
		s.add(" * @see jgralab.Vertex:isValidOemga()");
		s.add(" */");
		s.add("public boolean isValidOmega(Edge edge) {");
		s.add("\treturn validToEdges.contains(edge.getClass());");
		s.add("}");
		s.add("");
		s.add("{");
		code.addNoIndent(s);
		for (EdgeClass ec : vc.getValidToEdgeClasses()) {
			CodeSnippet line = new CodeSnippet(true);
			line.setVariable("edgeClassQualifiedName", schemaRootPackageName + ".impl." + (currentCycle.isTransImpl() ?  "trans." : "std.") + ec.getQualifiedName());
			line.add("\tvalidToEdges.add(#edgeClassQualifiedName#Impl.class);");
			code.addNoIndent(line);
		}
		s = new CodeSnippet(true);
		s.add("}");
		code.addNoIndent(s);
		return code;
	}

}
