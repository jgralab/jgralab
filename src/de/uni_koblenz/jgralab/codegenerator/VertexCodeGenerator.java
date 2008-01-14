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

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.RolenameEntry;
import de.uni_koblenz.jgralab.VertexClass;

/**
 * This class is used by the method Schema.commit() to generate the 
 * Java-classes that implement the VertexClasses of a graph schema. 
 *
 */
public class VertexCodeGenerator extends AttributedElementCodeGenerator {

	
	public VertexCodeGenerator(VertexClass vertexClass,
			String schemaPackageName, String implementationName) {
		super(vertexClass, schemaPackageName, implementationName);
		rootBlock.setVariable("graphElementClass", "Vertex");
	}

	/**
	 * creates the header of the classfile, that is the part
	 * <code>public class VertexClassName extends Vertex {</code> 
	 */
	protected CodeBlock createHeader(boolean createClass) {
		addImports("#jgPackage#.Vertex");
		return super.createHeader(createClass);
	}

	/**
	 * creates the body of the classfile,
	 * that are methods and attributes
	 */
	protected CodeBlock createBody(boolean createClass) {
		CodeList code = (CodeList) super.createBody(createClass);
		if (createClass) {
			addImports("#jgImplPackage#.#jgImplementation#.VertexImpl");
			rootBlock.setVariable("baseClassName", "VertexImpl");
			code.add(createValidEdgeSets((VertexClass) aec));
		}
		code.add(createNextVertexMethods(createClass));
		code.add(createFirstEdgeMethods(createClass));
		code.add(createRolenameMethods(createClass));
		code.add(createIncidenceIteratorMethods(createClass));
		return code;
	}

	/**
	 * creates the methods <code>getFirstEdgeName()</code>
	 * @param createClass iff set to true, the method bodies will also be created 
	 * @return the CodeBlock that contains the methods
	 */
	private CodeBlock createFirstEdgeMethods(boolean createClass) {
		CodeList code = new CodeList();
		for (EdgeClass ec : ((VertexClass) aec).getEdgeClasses()) {
			if (ec.isInternal()) {
				continue;
			}
			addImports("#jgPackage#.EdgeDirection");
			if (createClass) {
				addImports("#schemaPackage#." + ec.getName());
			}
			code.addNoIndent(createFirstEdgeMethod(ec, false, false,
					createClass));
			code.addNoIndent(createFirstEdgeMethod(ec, true, false,
					createClass));

			if (!ec.isAbstract()) {
				code.addNoIndent(createFirstEdgeMethod(ec, false, true,
						createClass));
				code.addNoIndent(createFirstEdgeMethod(ec, true, true,
						createClass));
			}
		}
		return code;
	}

	/**
	 * creates the method <code>getFirstEdgeName()</code> for the given
	 * EdgeClass
	 * @param createClass iff set to true, the method bodies will also be created
	 * @param withOrientation toggles if the EdgeDirection-parameter will be created
	 * @param withTypeFlag toggles if the "no subclasses"-parameter will be created
	 * @return the CodeBlock that contains the method
	 */
	private CodeBlock createFirstEdgeMethod(EdgeClass ec,
			boolean withOrientation, boolean withTypeFlag, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
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
		if (!createClass) {
			code.add("/**",
					" * @return the first edge of class #ecName# at this vertex");

			if (withOrientation) {
				code.add(" * @param orientation the orientation of the edge");
			}
			if (withTypeFlag) {
				code.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #ecName# are accepted");
			}
			code.add(" */",
					"public #ecName# getFirst#ecCamelName#(#formalParams#);");
		} else {
			code.add(
					"public #ecName# getFirst#ecCamelName#(#formalParams#) {",
					"\treturn (#ecName#)getFirstEdgeOfClass(#ecName#.class#actualParams#);",
					"}");

		}
		return code;
	}

	/**
	 * creates the <code>getNextVertexClassName()</code> methods
	 * @param createClass iff set to true, also the method bodies will be created
	 * @return the CodeBlock that contains the methods
	 */
	private CodeBlock createNextVertexMethods(boolean createClass) {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		for (AttributedElementClass ec : superClasses) {
			if (ec.isInternal()) {
				continue;
			}
			VertexClass vc = (VertexClass) ec;
			code.addNoIndent(createNextVertexMethod(vc, false, createClass));
			if (!vc.isAbstract()) {
				code.addNoIndent(createNextVertexMethod(vc, true, createClass));
			}
		}
		return code;
	}

	/**
	 * creates the <code>getNextVertexClassName()</code> method for the given
	 * VertexClass
	 * @param createClass iff set to true, the method bodies will also be created
	 * @param withTypeFlag toggles if the "no subclasses"-parameter will be created
	 * @return the CodeBlock that contains the method
	 */
	private CodeBlock createNextVertexMethod(VertexClass vc,
			boolean withTypeFlag, boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("vcName", vc.getName());
		code.setVariable("vcCamelName", camelCase(vc.getName()));
		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"	: ""));
		code.setVariable("actualParams", (withTypeFlag ? ", noSubClasses" : ""));

		if (!createClass) {
			code.add("/**"," * @return the next #vcName# vertex in the global vertex sequence");
			if (withTypeFlag) {
				code.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #vcName# are accepted");
			}
			code.add(" */","public #vcName# getNext#vcCamelName#(#formalParams#);");
		} else {
			code.add(
					"public #vcName# getNext#vcCamelName#(#formalParams#) {",
					"\treturn (#vcName#)getNextVertexOfClass(#vcName#.class#actualParams#);",
					"}");
		}
		return code;
	}


	/**
	 * creates the <code>getRolenameList()</code> methods for the current vertex.
	 * @param createClass iff set to true, also the method bodies will be created
	 * @return the CodeBlock that contains the code for the getRolenameList-methods
	 */
	private CodeBlock createRolenameMethods(boolean createClass) {
		VertexClass vc = (VertexClass) aec;
		/* all roles that are valid, that means not redefined, at the current edge class */
		Map<String, RolenameEntry> validRoles = vc.getValidRolenameMap();
		/* the roles to generate code for are either only the direct ones (if only the
		 * interface code is generated or all valid ones if the clas code is generated */
		Map<String, RolenameEntry> rolesToGenerate = vc.getOwnRolenameMap();
		if (createClass)
			rolesToGenerate = validRoles;
		
		CodeList code = new CodeList();
		for (Entry<String, RolenameEntry> entry : rolesToGenerate.entrySet()) {
			CodeSnippet s = configureRolenameCodesnippet(entry.getKey(), entry.getValue(), createClass);
			if (s == null)
				continue;
			if (!createClass) {
				s.add("public java.util.List<EdgeVertexPair<?extends #ecName#, ? extends #targetClass#>> get#roleCamelName#List();");
			} else {
				s.add("public java.util.List<EdgeVertexPair<?extends #ecName#, ? extends #targetClass#>> get#roleCamelName#List() {",
					  "\tjava.util.List<EdgeVertexPair<?extends #ecName#, ? extends #targetClass#>> list = new java.util.ArrayList<EdgeVertexPair<?extends #ecName#, ? extends #targetClass#>>();",
					  "\t#ecName# edge = getFirst#ecCamelName#(#dir#);",
					  "\twhile (edge != null) {");
				s.add("\t\tif (edge.getThatRole().equals(\"#roleName#\")) {");
				s.add("\t\t\tEdgeVertexPair<? extends #ecName#, ? extends #targetClass#> pair = new EdgeVertexPair<#ecName#,  #targetClass#>((#ecName#)edge, (#targetClass#) edge.getThat());",
						"\t\t\tlist.add(pair);");
				s.add("\t\t}");
				s.add("\t\tedge = edge.getNext#ecCamelName#(#dir#);", "\t}",
						"\treturn list;", "}");
			}
			code.addNoIndent(s);
		}
		if (createClass) {
			/*
			 * if code for the class is generated, for every invalid role 
			 * the methods have to be implemented in a way that they throw
			 * an exception everytime they are called
			 */
			for (Entry<String, RolenameEntry> entry : vc.getInvalidRolenameMap().entrySet()) {
				CodeSnippet s = configureRolenameCodesnippet(entry.getKey(), entry.getValue(), createClass);
				if (s == null)
					continue;
				addImports("#jgPackage#.GraphException");
				s.setVariable("vertexClassName", vc.getName());
				code.addNoIndent(s);
				s.add("public java.util.List<EdgeVertexPair<?extends #ecName#, ? extends #targetClass#>> get#roleCamelName#List() {");
				s.add("\tthrow new GraphException(\"The rolename #roleName# is redefined for the VertexClass #vertexClassName#\");");
				s.add("}");
			}
		}
		return code;
	}
	
	/**
	 * Creates a codeSnippet
	 * @param codeList The CodeList that represents the code for all Rolename Methods
	 * @param entry the pair <Rolename, Set<EdgeClassTriple>> to generate code for
	 * @param allRoles all roles that are related to the VertexClass currently creating code for
	 * @param createClass toggles wether to create code for the class or for the interface 
	 * @return 
	 */
	private CodeSnippet configureRolenameCodesnippet(String rolename, RolenameEntry entry, boolean createClass) {
		CodeSnippet s = new CodeSnippet(true);
		String targetClassName = entry.getLeastCommonVertexClass().getName();
		s.setVariable("roleName", rolename);
		s.setVariable("roleCamelName", camelCase(rolename));
		s.setVariable("targetClass", targetClassName);
		s.setVariable("dir", "");
		if (createClass) {
			if (entry.getLeastCommonEdgeClass().isInternal())
				addImports("#jgPackage#." + entry.getLeastCommonEdgeClass().getName());
			else
				addImports("#schemaPackage#." + entry.getLeastCommonEdgeClass().getName());
			if (targetClassName.equals("Vertex")) {
				addImports("#jgPackage#.Vertex");
			} else {
				addImports("#schemaPackage#." + targetClassName);
			}
		}	
		EdgeClass lcec = entry.getLeastCommonEdgeClass();
		if (lcec.isInternal()) {
			addImports("#jgPackage#." + lcec.getName());
		} else {
			addImports("#schemaPackage#." + lcec.getName());
		}	
		s.setVariable("ecName", lcec.getName());
		s.setVariable("ecCamelName", camelCase(lcec.getName()));
		return s;
	}

	/**
	 * creates the <code>getEdgeNameIncidences</code> methods
	 * @param createClass iff set to true, also the method bodies will be created
	 * @return the CodeBlock that contains the code for the getEdgeNameIncidences-methods
	 */
	private CodeBlock createIncidenceIteratorMethods(boolean createClass) {
		VertexClass vc = (VertexClass) aec;

		CodeList code = new CodeList();

		Set<EdgeClass> edgeClassSet = null;
		if (createClass) {
			edgeClassSet = vc.getEdgeClasses();
		} else {
			edgeClassSet = vc.getOwnDirectEdgeClasses();
		}

		for (EdgeClass edge : edgeClassSet) {
			if (edge.isInternal())
				continue;

			addImports("#jgPackage#.EdgeVertexPair");
			if (createClass)
				addImports("#jgImplPackage#.IncidenceIterable");

			CodeSnippet s = new CodeSnippet(true);
			code.addNoIndent(s);

			String targetClassName = edge.getName();
			s.setVariable("edgeClassName", targetClassName);

			/* getFooIncidences() */
			if (!createClass) {
				s.add("/**");
				s.add(" * Returns an iterable for all incidence edges of this vertex that are of type #edgeClassName# or subtypes");
				s.add(" */");
				s.add("public Iterable<EdgeVertexPair<? extends #edgeClassName#, ? extends Vertex>> get#edgeClassName#Incidences();");
			} else {
				s.add("public Iterable<EdgeVertexPair<? extends #edgeClassName#, ? extends Vertex>> get#edgeClassName#Incidences() {");
				s.add("\treturn new IncidenceIterable<#edgeClassName#, Vertex>(this, #edgeClassName#.class);");
				s.add("}");
			}
			s.add("");
			/* getFooIncidences(boolean nosubclasses) */
			if (!createClass) {
				s.add("/**");
				s.add(" * Returns an iterable for all incidence edges of this vertex that are of type #edgeClassName#");
				s.add(" * @param noSubClasses toggles wether subclasses of #edgeClassName# should be excluded");
				s.add(" */");
				s.add("public Iterable<EdgeVertexPair<? extends #edgeClassName#, ? extends Vertex>> get#edgeClassName#Incidences(boolean noSubClasses);");
			} else {
				s.add("public Iterable<EdgeVertexPair<? extends #edgeClassName#, ? extends Vertex>> get#edgeClassName#Incidences(boolean noSubClasses) {");
				s.add("\treturn new IncidenceIterable<#edgeClassName#, Vertex>(this, #edgeClassName#.class, noSubClasses);");
				s.add("}\n");
			}

			/* getFooIncidences(EdgeDirection direction, boolean nosubclasses) */
			if (!createClass) {
				s.add("/**");
				s.add(" * Returns an iterable for all incidence edges of this vertex that are of type #edgeClassName#");
				s.add(" * @param direction EdgeDirection.IN or EdgeDirection.OUT, only edges of this direction will be included in the iterable");
				s.add(" * @param noSubClasses toggles wether subclasses of #edgeClassName# should be excluded");
				s.add(" */");
				s.add("public Iterable<EdgeVertexPair<? extends #edgeClassName#, ? extends Vertex>> get#edgeClassName#Incidences(EdgeDirection direction, boolean noSubClasses);");
			} else {
				s.add("public Iterable<EdgeVertexPair<? extends #edgeClassName#, ? extends Vertex>> get#edgeClassName#Incidences(EdgeDirection direction, boolean noSubClasses) {");
				s.add("\treturn  new IncidenceIterable<#edgeClassName#, Vertex>(this, #edgeClassName#.class, direction, noSubClasses);");
				s.add("}");
			}
			s.add("");
			/* getFooIncidences(EdgeDirection direction) */
			if (!createClass) {
				s.add("/**");
				s.add(" * Returns an iterable for all incidence edges of this vertex that are of type #edgeClassName#");
				s.add(" * @param direction EdgeDirection.IN or EdgeDirection.OUT, only edges of this direction will be included in the iterable");
				s.add(" */");
				s.add("public Iterable<EdgeVertexPair<? extends #edgeClassName#, ? extends Vertex>> get#edgeClassName#Incidences(EdgeDirection direction);");
			} else {
				s.add("public Iterable<EdgeVertexPair<? extends #edgeClassName#, ? extends Vertex>> get#edgeClassName#Incidences(EdgeDirection direction) {");
				s.add("\treturn new IncidenceIterable<#edgeClassName#, Vertex>(this, #edgeClassName#.class, direction);");
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
		code.setVariable("vcName", vc.getName());
		code.setVariable("vcCamelName", camelCase(vc.getName()));
		CodeSnippet s = new CodeSnippet(true);
		s.add("/* add all valid from edges */");
		s.add("private static Set<Class<? extends Edge>> validFromEdges = new HashSet<Class<? extends Edge>>();");
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
			line.setVariable("edgeClassName", ec.getName());
			line.add("\tvalidFromEdges.add(#edgeClassName#Impl.class);");
			code.addNoIndent(line);
		}
		s = new CodeSnippet(true);
	    s.add("}");
	    s.add("");
		s.add("/* add all valid to edges */");
		s.add("private static Set<Class<? extends Edge>> validToEdges = new HashSet<Class<? extends Edge>>();"); 
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
			line.setVariable("edgeClassName", ec.getName());
			line.add("\tvalidToEdges.add(#edgeClassName#Impl.class);");
			code.addNoIndent(line);
		}
		s = new CodeSnippet(true);
		s.add("}");
		code.addNoIndent(s);
		return code;
	}

}
