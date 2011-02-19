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

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.IncidenceDirection;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class RolenameCodeGenerator {

	private VertexClass vertexClass;

	private String schemaRootPackageName;

	RolenameCodeGenerator(VertexClass vertexClass) {
		this.vertexClass = vertexClass;
		schemaRootPackageName = vertexClass.getSchema().getPackagePrefix()
				+ ".";
	}

	private CodeBlock createRemoveAdjacenceSnippet(String rolename,
			EdgeClass edgeClass, VertexClass definingVertexClass,
			EdgeDirection dir, boolean createClass) {
		CodeSnippet code = new CodeSnippet();
		code.setVariable("rolename", rolename);
		code.setVariable("edgeClassName", schemaRootPackageName
				+ edgeClass.getQualifiedName());
		code.setVariable("dir", "EdgeDirection." + dir.toString());
		code.setVariable("definingVertexClassName", schemaRootPackageName
				+ definingVertexClass.getQualifiedName());
		if (!createClass) {
			code
					.add(
							"/**",
							" * removes the given vertex as <code>#rolename#</code> from this vertex, i.e. ",
							" * deletes the <code>#edgeClassName#</code> edge connections of this vertex with ",
							" * the given one.", " */",
							"public boolean remove_#rolename#(#definingVertexClassName# vertex);");
		} else {
			code
					.add(
							"@Override",
							"public boolean remove_#rolename#(#definingVertexClassName# vertex) {",
							"\tboolean elementRemoved = false;",
							"\t#edgeClassName# edge = (#edgeClassName#) getFirstIncidence(#edgeClassName#.class, #dir#);",
							"\twhile (edge != null) {",
							"\t\t#edgeClassName# next = (#edgeClassName#) edge.getNextIncidence(#edgeClassName#.class, #dir#);",
							"\t\tif (edge.getThat().equals(vertex)) {"
									+ "\t\t\tedge.delete();",
							"\t\t\telementRemoved = true;", "\t\t}",
							"\t\tedge = next;", "\t}",
							"\treturn elementRemoved;", "}");
		}
		return code;
	}

	private CodeBlock createRemoveAllAdjacencesSnippet(String rolename,
			EdgeClass edgeClass, VertexClass otherVertexClass,
			EdgeDirection dir, boolean createClass) {
		CodeSnippet code = new CodeSnippet();
		code.setVariable("rolename", rolename);
		code.setVariable("edgeClassName", schemaRootPackageName
				+ edgeClass.getQualifiedName());
		code.setVariable("dir", "EdgeDirection." + dir.toString());
		code.setVariable("vertexClassName", schemaRootPackageName
				+ otherVertexClass.getQualifiedName());
		if (!createClass) {
			code
					.add(
							"/**",
							" * removes all #rolename# adjacences to all vertices by ",
							" * deleting the <code>#edgeClassName#</code> edges of this vertex to ",
							" * all other ones, but doesn't delete those vertices.",
							" *",
							" * @return the adjacent vertices prior to removal of incidences",
							" */",
							"public java.util.List<? extends #vertexClassName#> remove_#rolename#();");
		} else {
			code
					.add(
							"@Override",
							"public java.util.List<? extends #vertexClassName#> remove_#rolename#() {",
							"\tjava.util.List<#vertexClassName#> adjacences = new java.util.ArrayList<#vertexClassName#>();",
							"\t#edgeClassName# edge = (#edgeClassName#) getFirstIncidence(#edgeClassName#.class, #dir#);",
							"\twhile (edge != null) {",
							"\t\t#edgeClassName# next = (#edgeClassName#) edge.getNextIncidence(#edgeClassName#.class, #dir#);",
							"\t\tadjacences.add((#vertexClassName#) edge.getThat());",
							"\t\tedge.delete();", "\t\tedge = next;", "\t}",
							"\treturn adjacences;", "}");
		}
		return code;
	}

	private CodeBlock createGetAdjacencesSnippet(IncidenceClass incClass,
			VertexClass allowedVertexClass, EdgeDirection dir,
			boolean createClass) {
		CodeSnippet code = new CodeSnippet();
		code.setVariable("rolename", incClass.getRolename());
		code.setVariable("edgeClassName", schemaRootPackageName
				+ incClass.getEdgeClass().getQualifiedName());
		code.setVariable("dir", "EdgeDirection." + dir.toString());
		code.setVariable("vertexClassName", schemaRootPackageName
				+ allowedVertexClass.getQualifiedName());

		if (incClass.getMax() == 1) {
			// if the rolename has an upper multiplicity of 1, create a method
			// to access just the one element
			if (!createClass) {
				code
						.add(
								"/**",
								" * @return the vertex to this one with the rolename '#rolename#' ",
								" *         (connected with a <code>#edgeClassName#</code> edge), or null if no such vertex exists",
								" */",
								"public #vertexClassName# get_#rolename#();");
			} else {
				code
						.add(
								"@Override",
								"public #vertexClassName# get_#rolename#() {",
								"\t#edgeClassName# edge = (#edgeClassName#) getFirstIncidence(#edgeClassName#.class, #dir#);",
								"\tif (edge != null) {",
								"\t\treturn (#vertexClassName#) edge.getThat();",
								"\t}", "\treturn null;", "}");
			}
		} else {
			// if the rolename has an upper multiplicity greater than 1, create
			// a method to access the list of elements
			if (!createClass) {
				code
						.add(
								"/**",
								" * @return an Iterable of all vertices adjacent to this one with the rolename '#rolename#'",
								" *         (connected with a <code>#edgeClassName#</code> edge).",
								" */",
								"public Iterable<? extends #vertexClassName#> get_#rolename#();");
			} else {
				code
						.add(
								"@Override",
								"public Iterable<? extends #vertexClassName#> get_#rolename#() {",
								"\treturn new de.uni_koblenz.jgralab.impl.NeighbourIterable<#edgeClassName#, #vertexClassName#>(this, #edgeClassName#.class, #dir#);",
								"}");
			}
		}
		return code;
	}

	private CodeBlock createAddRolenameSnippet(String rolename,
			EdgeClass edgeClass, VertexClass definingVertexClass,
			VertexClass allowedVertexClass, EdgeDirection dir,
			boolean createClass) {
		CodeSnippet code = new CodeSnippet();
		code.setVariable("rolename", rolename);
		code.setVariable("edgeClassName", schemaRootPackageName
				+ edgeClass.getQualifiedName());
		code.setVariable("graphClassName", schemaRootPackageName
				+ edgeClass.getGraphClass().getQualifiedName());
		code.setVariable("definingVertexClassName", schemaRootPackageName
				+ definingVertexClass.getQualifiedName());
		code.setVariable("allowedVertexClassName", schemaRootPackageName
				+ allowedVertexClass.getQualifiedName());
		code.setVariable("thisVertexClassName", schemaRootPackageName
				+ vertexClass.getQualifiedName());
		if (dir == EdgeDirection.OUT) {
			code.setVariable("alpha", "this");
			code.setVariable("alphaVertexClassName", schemaRootPackageName
					+ vertexClass.getQualifiedName());
			code.setVariable("omega", "vertex");
			code.setVariable("omegaVertexClassName", schemaRootPackageName
					+ allowedVertexClass.getQualifiedName());
		} else {
			code.setVariable("alpha", "vertex");
			code.setVariable("alphaVertexClassName", schemaRootPackageName
					+ allowedVertexClass.getQualifiedName());
			code.setVariable("omega", "this");
			code.setVariable("omegaVertexClassName", schemaRootPackageName
					+ vertexClass.getQualifiedName());
		}
		if (!createClass) {
			code
					.add(
							"/**",
							" * adds the given vertex as <code>#rolename#</code> to this vertex, i.e. creates an",
							" * <code>#edgeClassName#</code> edge from this vertex to the given ",
							" * one and returns the created edge.",
							" * @return  a newly created edge of type <code>#edgeClassName#</code>",
							" *          between this vertex and the given one.",
							" */",
							"public #edgeClassName# add_#rolename#(#definingVertexClassName# vertex);");
		} else {
			code
					.add("@Override",
							"public #edgeClassName# add_#rolename#(#definingVertexClassName# vertex) {");
			if (definingVertexClass != allowedVertexClass) {
				code
						.add(
								"\tif (!(vertex instanceof #allowedVertexClassName#)) {",
								"\t\tthrow new de.uni_koblenz.jgralab.GraphException(\"The rolename #rolename# was redefined at the vertex class #thisVertexClassName#. Only vertices of #allowedVertexClassName# are allowed.\"); ",
								"\t}");
			}
			code
					.add(
							"\treturn ((#graphClassName#)getGraph()).createEdge(#edgeClassName#.class, (#alphaVertexClassName#) #alpha#, (#omegaVertexClassName#) #omega#);",
							"}");
		}
		return code;
	}

	public CodeBlock createRolenameMethods(boolean createClass) {
		CodeList list = new CodeList();
		Set<IncidenceClass> validFarICs = new HashSet<IncidenceClass>();
		validFarICs.addAll(vertexClass.getValidFromFarIncidenceClasses());
		validFarICs.addAll(vertexClass.getValidToFarIncidenceClasses());
		for (IncidenceClass ic : validFarICs) {
			list.add(createMethodsForOneIncidenceClass(ic, ic, createClass));
			for (IncidenceClass redefinedIC : ic.getRedefinedIncidenceClasses()) {
				list.add(createMethodsForOneIncidenceClass(ic, redefinedIC,
						createClass));
			}
		}
		return list;
	}

	private CodeList createMethodsForOneIncidenceClass(
			IncidenceClass allowedIncidenceClass,
			IncidenceClass definingIncidenceClass, boolean createClass) {
		CodeList list = new CodeList();
		String rolename = definingIncidenceClass.getRolename();
		if (!rolename.isEmpty()) {
			EdgeClass ec = allowedIncidenceClass.getEdgeClass();
			VertexClass vc = definingIncidenceClass.getVertexClass();
			VertexClass allowedVC = allowedIncidenceClass.getVertexClass();
			EdgeDirection dir = allowedIncidenceClass.getDirection() == IncidenceDirection.OUT ? EdgeDirection.IN
					: EdgeDirection.OUT;
			list.addNoIndent(createAddRolenameSnippet(rolename, ec, vc,
					allowedVC, dir, createClass));
			list.addNoIndent(createRemoveAllAdjacencesSnippet(rolename, ec,
					allowedVC, dir, createClass));
			list.addNoIndent(createRemoveAdjacenceSnippet(rolename, ec, vc,
					dir, createClass));
			list.addNoIndent(createGetAdjacencesSnippet(definingIncidenceClass,
					allowedVC, dir, createClass));
		}
		return list;
	}

}
