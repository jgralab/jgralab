/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
/**
 *
 */
package de.uni_koblenz.jgralab.greql.optimizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.funlib.logics.And;
import de.uni_koblenz.jgralab.greql.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql.schema.FunctionId;
import de.uni_koblenz.jgralab.greql.schema.GreqlAggregation;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql.schema.ThisLiteral;
import de.uni_koblenz.jgralab.greql.schema.Variable;

/**
 * Holds various static methods used by {@link Optimizer}s.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class OptimizerUtility {
	/**
	 * Checks if <code>v1</code> is above <code>v2</code> in the
	 * {@link GreqlGraph}. The {@link GreqlExpression} is considered to be above
	 * all other vertices, meaning the root is the top, too. The
	 * {@link GreqlAggregation}s in the syntaxgraph point in up-direction.
	 * 
	 * The <code>isAbove</code> relation is reflexive, e.g.
	 * <code>isAbove(v1, v1)</code> returns <code>true</code>.
	 * 
	 * @param v1
	 *            a {@link Vertex}
	 * @param v2
	 *            a {@link Vertex}
	 * @return <code>true</code> if v1 is above or == v2, <code>false</code>
	 *         otherwise
	 */
	public static boolean isAbove(Vertex v1, Vertex v2) {
		// GreqlEvaluator.println("isAbove(" + v1 +", " + v2 +")");
		if (v1 == v2) {
			return true;
		}
		for (Edge inc : v1.incidences(EdgeDirection.IN)) {
			if (isAbove(inc.getAlpha(), v2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if <code>funApp</code> is an AND {@link FunctionApplication}.
	 * 
	 * @param funApp
	 *            a {@link FunctionApplication}
	 * @return <code>true</code> if <code>funApp</code> is a
	 *         {@link FunctionApplication} of {@link And}.
	 */
	public static boolean isAnd(FunctionApplication funApp) {
		return funApp.getFirstIsFunctionIdOfIncidence().getAlpha().get_name()
				.equals("and");
	}

	/**
	 * Check if <code>funApp</code> is an OR {@link FunctionApplication}.
	 * 
	 * @param funApp
	 *            a {@link FunctionApplication}
	 * @return <code>true</code> if <code>funApp</code> is a
	 *         {@link FunctionApplication} of {@link And}.
	 */
	public static boolean isOr(FunctionApplication funApp) {
		return funApp.getFirstIsFunctionIdOfIncidence().getAlpha().get_name()
				.equals("or");
	}

	/**
	 * Check if <code>funApp</code> is an XOR {@link FunctionApplication}.
	 * 
	 * @param funApp
	 *            a {@link FunctionApplication}
	 * @return <code>true</code> if <code>funApp</code> is a
	 *         {@link FunctionApplication} of {@link And}.
	 */
	public static boolean isXor(FunctionApplication funApp) {
		return funApp.getFirstIsFunctionIdOfIncidence().getAlpha().get_name()
				.equals("xor");
	}

	/**
	 * Check if <code>funApp</code> is an NOT {@link FunctionApplication}.
	 * 
	 * @param funApp
	 *            a {@link FunctionApplication}
	 * @return <code>true</code> if <code>funApp</code> is a
	 *         {@link FunctionApplication} of {@link And}.
	 */
	public static boolean isNot(FunctionApplication funApp) {
		return funApp.getFirstIsFunctionIdOfIncidence().getAlpha().get_name()
				.equals("not");
	}

	/**
	 * Merges the contents of the sourcePosition attribute of <code>from</code>
	 * to the contents of the sourcePosition attribute of <code>to</code>. If a
	 * {@link SourcePosition} already exists in <code>to</code> it won't be
	 * added again.
	 * 
	 * @param from
	 *            a {@link GreqlAggregation}
	 * @param to
	 *            another {@link GreqlAggregation}
	 */
	public static void mergeSourcePositions(GreqlAggregation from,
			GreqlAggregation to) {
		PVector<SourcePosition> toSourcePositions = to.get_sourcePositions();
		if (toSourcePositions == null) {
			toSourcePositions = JGraLab.vector();
		}
		for (SourcePosition sp : from.get_sourcePositions()) {
			if (!toSourcePositions.contains(sp)) {
				toSourcePositions = toSourcePositions.plus(sp);
			}
		}
		to.set_sourcePositions(toSourcePositions);
	}

	/**
	 * Find the {@link FunctionId} in the {@link GreqlGraph} that has
	 * <code>name</code> as its name attribute. If no such {@link FunctionId}
	 * exists it will be created.
	 * 
	 * @param name
	 *            the value of the name attribute of the {@link FunctionId} 
	 *            we're looking for
	 * @param graph
	 *            the {@link GreqlGraph} where we look for the
	 *            {@link FunctionId}
	 * @return the {@link FunctionId} in the {@link GreqlGraph} that has
	 *         <code>name</code> as its name attribute. If no such
	 *         {@link FunctionId} exists it will be created.
	 */
	public static FunctionId findOrCreateFunctionId(String name,
			GreqlGraph graph) {
		for (FunctionId fid : graph.getFunctionIdVertices()) {
			if (fid.get_name().equals(name)) {
				return fid;
			}
		}
		// no such FunctionId exists, so create one
		FunctionId fid = graph.createFunctionId();
		fid.set_name(name);
		return fid;
	}

	/**
	 * Initialize all sourcePosition attributes of <code>graph</code> that are
	 * <code>null</code> with an empty {@link ArrayList}.
	 * 
	 * @param graph
	 *            the {@link GreqlGraph}
	 */
	public static void createMissingSourcePositions(GreqlGraph graph) {
		for (GreqlAggregation aggr : graph.getGreqlAggregationEdges()) {
			if (aggr.get_sourcePositions() == null) {
				PVector<SourcePosition> l = JGraLab.vector();
				aggr.set_sourcePositions(l);
			}
		}
	}

	/**
	 * @param sd
	 *            a {@link SimpleDeclaration}
	 * @return a {@link Set} of all {@link Variable}s declared by
	 *         <code>sd</code>
	 */
	public static Set<Variable> collectVariablesDeclaredBy(SimpleDeclaration sd) {
		HashSet<Variable> vars = new HashSet<>();
		for (IsDeclaredVarOf inc : sd
				.getIsDeclaredVarOfIncidences(EdgeDirection.IN)) {
			vars.add(inc.getAlpha());
		}
		return vars;
	}

	/**
	 * Collect all of {@link Variable}s that are located below <code>v</code>,
	 * and that are declared in the current query (not bound variables of the
	 * expression).
	 * 
	 * @param vertex
	 *            the root {@link Vertex} below which to look for
	 *            {@link Variable}s
	 * @return a {@link Set} of {@link Variable}s that are located below
	 *         <code>v</code>
	 */
	public static Set<Variable> collectInternallyDeclaredVariablesBelow(
			Vertex vertex) {
		return collectInternallyDeclaredVariablesBelow(vertex,
				new HashSet<Variable>());
	}

	/**
	 * Add all {@link Variable} vertices to <code>vars</code> that are in the
	 * subgraph below <code>vertex</code>, and that are declared in the current
	 * query (not bound variables of the expression). Return <code>vars</code>.
	 * 
	 * @param vertex
	 * @param vars
	 * @return the set of {@link Variable} vertices that are located in the
	 *         subgraph below <code>vertex</code>
	 */
	private static Set<Variable> collectInternallyDeclaredVariablesBelow(
			Vertex vertex, Set<Variable> vars) {
		// GreqlEvaluator.println("collectVariablesBelow(" + vertex + ")");
		if ((vertex instanceof Variable) && !(vertex instanceof ThisLiteral)) {
			Variable v = (Variable) vertex;
			if (v.getFirstIsBoundVarOfIncidence(EdgeDirection.OUT) == null) {
				// it's no externally bound variable, but a variable declared in
				// that query...
				vars.add(v);
			}
			return vars;
		}
		for (Edge inc : vertex.incidences(EdgeDirection.IN)) {
			// GreqlEvaluator.println(inc + " <-- " + inc.getAlpha());
			collectInternallyDeclaredVariablesBelow(inc.getAlpha(), vars);
		}
		return vars;
	}

	/**
	 * Recursively delete all orphaned vertices below <code>vertex</code> except
	 * vertices in <code>verticesToOmit</code> and their subgraphs. A
	 * {@link Vertex} is considered orphaned if no {@link Edge} starts at it.
	 * 
	 * @param vertex
	 *            a {@link Vertex}
	 * @param verticesToOmit
	 *            a set of vertices whose subgraph shouldn't be deleted
	 */
	public static void deleteOrphanedVerticesBelow(Vertex vertex,
			HashSet<Vertex> verticesToOmit) {
		// System.out.println("deleteOrphanedVerticesBelow(" + vertex + ")");
		deleteOrphanedVerticesBelow(vertex, verticesToOmit,
				new HashSet<Vertex>());
	}

	private static void deleteOrphanedVerticesBelow(Vertex vertex,
			HashSet<Vertex> verticesToOmit,
			HashSet<Vertex> alreadyDeletedVertices) {
		assert vertex.isValid();
		if (alreadyDeletedVertices.contains(vertex)) {
			return;
		}

		HashSet<Vertex> nextOrphans = new HashSet<>();
		for (Edge inc : vertex.incidences(EdgeDirection.IN)) {
			nextOrphans.add(inc.getAlpha());
		}
		if ((vertex.getFirstIncidence(EdgeDirection.OUT) == null)
				&& !verticesToOmit.contains(vertex)) {
			// vertex is orphaned
			alreadyDeletedVertices.add(vertex);
			// System.out.println("deleting orphan " + vertex);
			vertex.delete();
			for (Vertex v : nextOrphans) {
				deleteOrphanedVerticesBelow(v, verticesToOmit,
						alreadyDeletedVertices);
			}
		}
	}
}
