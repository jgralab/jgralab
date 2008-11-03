package de.uni_koblenz.jgralab.codegenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.RolenameEntry;
import de.uni_koblenz.jgralab.schema.impl.VertexEdgeEntry;

public class RolenameCodeGenerator {

	private VertexClass vertexClass;

	RolenameCodeGenerator(VertexClass vertexClass) {
		this.vertexClass = vertexClass;
	}

	private CodeBlock validRolenameSnippet(CodeSnippet s, boolean createClass, Set<EdgeClass> connectedEdgeSet) {
		if (!createClass) {
			s.add(
				"/**",
				" * @return a List of all #targetSimpleName# vertices related to this by a <code>#roleName#</code> link.",
				" */",
				"public java.util.List<? extends #targetClass#> get#roleCamelName#List();");
			return s;
		} else {
			CodeList list = new CodeList();
			CodeSnippet s2 = new CodeSnippet();
			s2.setVariable("roleCamelName", s.getVariable("roleCamelName"));
			s2.add(
				"\n",
				"private static java.util.HashSet<Class<? extends Edge>> connected#roleCamelName#EdgeSet = new java.util.HashSet<Class<? extends Edge>>();",
				"{");
			list.addNoIndent(s2);
			for (EdgeClass ec : connectedEdgeSet) {
				s2 = new CodeSnippet();
				s2.setVariable("roleCamelName", s.getVariable("roleCamelName"));
				s2.setVariable("edgeName", ec.getSchema().getPackageName() + "." + ec.getQualifiedName());
				s2.add("connected#roleCamelName#EdgeSet.add(#edgeName#.class);");
				list.add(s2);
			}
			s2 = new CodeSnippet();
			s2.add("}"	);
			list.addNoIndent(s2);

			s.add(
				"public java.util.List<? extends #targetClass#> get#roleCamelName#List() {",
				"\tjava.util.List<#targetClass#> list = new java.util.ArrayList<#targetClass#>();",
				"\t#ecQualifiedName# edge = getFirst#ecCamelName#(#dir#);",
				"\twhile (edge != null) {",
				"\t\tif (connected#roleCamelName#EdgeSet.contains(edge.getM1Class())) {",
				"\t\t\tlist.add((#targetClass#)edge.getThat());",
				"\t\t}",
				"\t\tedge = edge.getNext#ecCamelName#(#dir#);",
				"\t}", "\treturn list;", "}");
			list.addNoIndent(s);
			return list;
		}
	}

	private CodeBlock invalidRolenameSnippet(CodeSnippet s) {
		s.add("public java.util.List<#targetClass#> get#roleCamelName#List() {");
		s.add("\tthrow new #jgPackage#.GraphException(\"The rolename #roleName# is redefined for the VertexClass #vertexClassName#\");");
		s.add("}");
		return s;
	}


	private CodeBlock validAddRolenameSnippet(CodeSnippet s, boolean createClass) {
		if (!createClass) {
			s.add(
				"/**",
				" * adds the given vertex as <code>#roleCamelName#</code> to this vertex, i.e. creates an",
				" * <code>#edgeClassName#</code> edge from this vertex to the given ",
				" * one and returns the created edge.",
				" * @return  a newly created edge of type <code>#edgeClassName#</code>",
				" *          between this vertex and the given one.",
				" */",
				"public #edgeClassName# add#roleCamelName#(#vertexClassName# vertex);");
		} else {
			s.add(
				"public #edgeClassName# add#roleCamelName#(#vertexClassName# vertex) {",
				"\treturn ((#graphClassName#)getGraph()).create#edgeClassUniqueName#(#fromVertex#, #toVertex#);", "}");
		}
		return s;
	}

	private CodeBlock invalidAddRolenameSnippet(CodeSnippet s, boolean createClass) {
		s.add(
				"public #edgeClassName# add#roleCamelName#(#vertexClassName# vertex) {",
				"\tthrow new SchemaException(\"No edges of class \" + #edgeClassName# + \"are allowed at this vertex\");", "}");
		return s;
	}


	private CodeBlock validRemoveRolenameSnippet(CodeSnippet s, boolean createClass ) {
		if (!createClass) {
			s.add(
				"/**",
				" * removes the given vertex as <code>#roleCamelName#</code> from this vertex, i.e. " +
				" * deletes the <code>#edgeClassName#</code> edge connection this vertex with " ,
				" * the given one. The given vertex is only deleted if the edge is a composition",
				" * which implies a existential dependency between the composition and the child vertex",
				" */",
				"public void remove#roleCamelName#(#vertexClassName# vertex);");
		} else {
			s.add(
				"public void remove#roleCamelName#(#vertexClassName# vertex) {",
				"    Edge e = getFirst#edgeClassUniqueName#();",
				"    while (e != null && e.getThat() == vertex) {",
				"        e.delete();",
				"        e = getFirst#edgeClassUniqueName#();",
				"    }",
				"    while (e != null) {",
				"        Edge f = e.getNextEdge();",
				"        while (f != null && f.getThat() == vertex) {",
				"           f.delete();",
				"           f = e.getNextEdge();",
				"        }",
				"        e = f;",
				"   }",
				"}");
		}
		return s;
	}

	private CodeBlock invalidRemoveRolenameSnippet(CodeSnippet s, boolean createClass) {
		s.add(
				"public #edgeClassName# remove#roleCamelName#(#vertexClassName# vertex) {",
				"\tthrow new SchemaException(\"There is no rolename \" + #roleCamelName# + \" allowed at this vertex\");", "}");
		return s;
	}


	/**
	 * Calculates the set of EdgeClasses that are direct or indirect subclasses of the given
	 * EdgeClass <code>superclass</code> and that do not redefine the given rolename <code>role</code>
	 * in the given {@link EdgeDirection} <code>dir</code>
	 * @return
	 */
	private Set<EdgeClass> getSubsettingSubclasses(EdgeClass superclass, String role, EdgeDirection dir) {
		Set<EdgeClass> returnSet = new HashSet<EdgeClass>();
		for (AttributedElementClass ac : superclass.getDirectSubClasses()) {
			EdgeClass ec = (EdgeClass) ac;
			if (dir == EdgeDirection.IN) {
				if (!ec.getRedefinedFromRoles().contains(role)) {
					returnSet.add(ec);
					returnSet.addAll(getSubsettingSubclasses(ec, role, dir));
				}
			} else {
				if (!ec.getRedefinedToRoles().contains(role)) {
					returnSet.add(ec);
					returnSet.addAll(getSubsettingSubclasses(ec, role, dir));
				}
			}
		}
		return returnSet;
	}


	/**
	 * Creates the <code>getRolenameList()</code>, <code>addRolename()</code>
	 * and <code>removeRolename()</code> methods for the current vertex.
	 *
	 * @param createClass
	 *            iff set to true, also the method bodies will be created
	 * @return the CodeBlock that contains the code for the
	 *         getRolenameList-methods
	 */
	CodeBlock createRolenameMethods(boolean createClass) {
		Map<String, RolenameEntry> rolesToGenerateGetters = vertexClass.getRolenameMap();
		/* build map of rolenames and associated sets of directed edge classes*/
		HashMap<String, Set<EdgeClass>> outgoingEdgeClassMap = new HashMap<String, Set<EdgeClass>>();
		HashMap<String, Set<EdgeClass>> incommingEdgeClassMap = new HashMap<String, Set<EdgeClass>>();
		for (RolenameEntry entry : rolesToGenerateGetters.values()) {
			HashSet<EdgeClass> incommingSet = new HashSet<EdgeClass>();
			HashSet<EdgeClass> outgoingSet = new HashSet<EdgeClass>();
			for (VertexEdgeEntry edgeEntry : entry.getVertexEdgeEntryList()) {
				if (edgeEntry.getEdge().isAbstract()) {
					continue;
				}
				if (edgeEntry.getDirection() == EdgeDirection.IN) {
					incommingSet.add(edgeEntry.getEdge());
					incommingSet.addAll(getSubsettingSubclasses(edgeEntry.getEdge(), entry.getRoleNameAtFarEnd(), EdgeDirection.IN));
				}
				if (edgeEntry.getDirection() == EdgeDirection.OUT) {
					outgoingSet.add(edgeEntry.getEdge());
					outgoingSet.addAll(getSubsettingSubclasses(edgeEntry.getEdge(), entry.getRoleNameAtFarEnd(), EdgeDirection.OUT));
				}
			}
			incommingEdgeClassMap.put(entry.getRoleNameAtFarEnd(), incommingSet);
			outgoingEdgeClassMap.put(entry.getRoleNameAtFarEnd(), outgoingSet);
		}


		CodeList code = new CodeList();
		for (RolenameEntry entry : rolesToGenerateGetters.values()) {
			CodeSnippet s = configureRolenameCodesnippet(entry, createClass);
			if (entry.isRedefined()) {
				code.addNoIndent(invalidRolenameSnippet(s));
			} else {
				Set<EdgeClass> paramSet = incommingEdgeClassMap.get(entry.getRoleNameAtFarEnd());
				if (paramSet.isEmpty()) {
					paramSet = outgoingEdgeClassMap.get(entry.getRoleNameAtFarEnd());
				}
				code.addNoIndent(validRolenameSnippet(s, createClass, paramSet));
			}
			for (VertexEdgeEntry edgeEntry : entry.getVertexEdgeEntryList()) {
				if (edgeEntry.getEdge().isAbstract()) {
					continue;
				}
				String schemaRootPackageName = entry.getEdgeClassToTraverse().getEdgeClass().getSchema().getPackageName();
				CodeSnippet addSnippet = new CodeSnippet(true);
				addSnippet.setVariable("roleCamelName", CodeGenerator.camelCase(entry.getRoleNameAtFarEnd()));
				addSnippet.setVariable("edgeClassName", schemaRootPackageName + "." + edgeEntry.getEdge().getQualifiedName());
				addSnippet.setVariable("edgeClassUniqueName", CodeGenerator.camelCase(edgeEntry.getEdge().getUniqueName()));
				addSnippet.setVariable("graphClassName", schemaRootPackageName + "." + edgeEntry.getEdge().getGraphClass().getQualifiedName());
				addSnippet.setVariable("vertexClassName", schemaRootPackageName + "." + edgeEntry.getVertex().getQualifiedName());

				if (edgeEntry.getDirection() == EdgeDirection.IN) {
					addSnippet.setVariable("fromVertex", "vertex");
					addSnippet.setVariable("toVertex", "this");
				} else {
					addSnippet.setVariable("toVertex", "vertex");
					addSnippet.setVariable("fromVertex", "this");
				}
				CodeSnippet removeSnippet = new CodeSnippet(true);
				removeSnippet.setVariable("roleCamelName", CodeGenerator.camelCase(entry.getRoleNameAtFarEnd()));
				removeSnippet.setVariable("edgeClassName", schemaRootPackageName + "." + edgeEntry.getEdge().getQualifiedName());
				removeSnippet.setVariable("edgeClassUniqueName", CodeGenerator.camelCase(edgeEntry.getEdge().getUniqueName()));
				removeSnippet.setVariable("vertexClassName", schemaRootPackageName + "." + edgeEntry.getVertex().getQualifiedName());
				if (!edgeEntry.isRedefined()) {
					code.addNoIndent(validAddRolenameSnippet(addSnippet, createClass));
					code.addNoIndent(validRemoveRolenameSnippet(removeSnippet, createClass));
				} else {
					code.addNoIndent(invalidAddRolenameSnippet(addSnippet, createClass));
					code.addNoIndent(invalidRemoveRolenameSnippet(removeSnippet, createClass));
				}
			}
		}
		return code;
	}

	/**
	 * Creates a codeSnippet
	 *
	 * @param codeList
	 *            The CodeList that represents the code for all Rolename Methods
	 * @param entry
	 *            the pair <Rolename, Set<EdgeClassTriple>> to generate code
	 *            for
	 * @param allRoles
	 *            all roles that are related to the VertexClass currently
	 *            creating code for
	 * @param createClass
	 *            toggles if to create code for the class or for the
	 *            interface
	 * @return
	 */
	private CodeSnippet configureRolenameCodesnippet(RolenameEntry entry, boolean createClass) {
		CodeSnippet s = new CodeSnippet(true);
		VertexClass lcvc = entry.getVertexClassAtFarEnd();
		if (lcvc.isInternal()) {
			s.setVariable("targetClass", "#jgPackage#." + lcvc.getQualifiedName());
		} else {
			s.setVariable("targetClass", lcvc.getSchema().getPackageName() + "."
					+ lcvc.getQualifiedName());
		}
		s.setVariable("targetSimpleName", lcvc.getSimpleName());
		s.setVariable("roleName", entry.getRoleNameAtFarEnd());
		s.setVariable("roleCamelName", CodeGenerator.camelCase(entry.getRoleNameAtFarEnd()));
		s.setVariable("dir", "EdgeDirection." + entry.getEdgeClassToTraverse().getDirection().toString());
		EdgeClass lcec = entry.getEdgeClassToTraverse().getEdgeClass();
		if (lcec.isInternal()) {
			s.setVariable("ecQualifiedName", "#jgPackage#."
					+ lcec.getSimpleName());
		} else {
			s.setVariable("ecQualifiedName", lcvc.getSchema().getPackageName() + "."
					+ lcec.getQualifiedName());
		}
		s.setVariable("ecCamelName", CodeGenerator.camelCase(lcec.getUniqueName()));
		return s;
	}

}
