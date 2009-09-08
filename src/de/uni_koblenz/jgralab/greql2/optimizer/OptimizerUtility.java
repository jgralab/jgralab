/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.funlib.And;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.schema.ThisLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * Holds various static methods used by {@link Optimizer}s.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class OptimizerUtility {

	/**
	 * Checks if <code>v1</code> is above <code>v2</code> in the {@link Greql2}
	 * syntaxgraph. The {@link Greql2Expression} is considered to be above all
	 * other vertices, meaning the root is the top, too. The
	 * {@link Greql2Aggregation}s in the syntaxgraph point in up-direction.
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
		try {
			return funApp.getFirstIsFunctionIdOf().getAlpha().getAttribute(
					"name").equals("and");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		}
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
		try {
			return funApp.getFirstIsFunctionIdOf().getAlpha().getAttribute(
					"name").equals("or");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		}
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
		try {
			return funApp.getFirstIsFunctionIdOf().getAlpha().getAttribute(
					"name").equals("xor");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		}
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
		try {
			return funApp.getFirstIsFunctionIdOf().getAlpha().getAttribute(
					"name").equals("not");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Merges the contents of the sourcePosition attribute of <code>from</code>
	 * to the contents of the sourcePosition attribute of <code>to</code>. If a
	 * {@link SourcePosition} already exists in <code>to</code> it won't be
	 * added again.
	 * 
	 * @param from
	 *            a {@link Greql2Aggregation}
	 * @param to
	 *            another {@link Greql2Aggregation}
	 */
	public static void mergeSourcePositions(Greql2Aggregation from,
			Greql2Aggregation to) {
		List<SourcePosition> toSourcePositions = to.getSourcePositions();
		if (toSourcePositions == null) {
			toSourcePositions = new ArrayList<SourcePosition>();
			to.setSourcePositions(toSourcePositions);
		}
		for (SourcePosition sp : from.getSourcePositions()) {
			if (!toSourcePositions.contains(sp)) {
				toSourcePositions.add(sp);
			}
		}
	}

	/**
	 * Find the {@link FunctionId} in the {@link Greql2} graph that has
	 * <code>name</code> as its name attribute. If no such {@link FunctionId}
	 * exists it will be created.
	 * 
	 * @param name
	 *            the value of the name attribute of the {@link FunctionId} 
	 *            we're looking for
	 * @param graph
	 *            the {@link Greql2} graph where we look for the
	 *            {@link FunctionId}
	 * @return the {@link FunctionId} in the {@link Greql2} graph that has
	 *         <code>name</code> as its name attribute. If no such
	 *         {@link FunctionId} exists it will be created.
	 */
	public static FunctionId findOrCreateFunctionId(String name, Greql2 graph) {
		for (FunctionId fid : graph.getFunctionIdVertices()) {
			if (fid.getName().equals(name)) {
				return fid;
			}
		}
		// no such FunctionId exists, so create one
		FunctionId fid = graph.createFunctionId();
		fid.setName(name);
		return fid;
	}

	/**
	 * Initialize all sourcePosition attributes of <code>graph</code> that are
	 * <code>null</code> with an empty {@link ArrayList}.
	 * 
	 * @param graph
	 *            the {@link Greql2} syntaxgraph
	 */
	public static void createMissingSourcePositions(Greql2 graph) {
		for (Greql2Aggregation aggr : graph.getGreql2AggregationEdges()) {
			if (aggr.getSourcePositions() == null) {
				aggr.setSourcePositions(new ArrayList<SourcePosition>());
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
		HashSet<Variable> vars = new HashSet<Variable>();
		for (IsDeclaredVarOf inc : sd
				.getIsDeclaredVarOfIncidences(EdgeDirection.IN)) {
			vars.add((Variable) inc.getAlpha());
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
		if (vertex instanceof Variable && !(vertex instanceof ThisLiteral)) {
			Variable v = (Variable) vertex;
			if (v.getFirstIsBoundVarOf(EdgeDirection.OUT) == null) {
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
			HashSet<? extends Vertex> verticesToOmit) {
		// GreqlEvaluator.println("deleteOrphanedVerticesBelow(" + vertex +
		// ")");
		deleteOrphanedVerticesBelow(vertex, verticesToOmit,
				new HashSet<Vertex>());
	}

	private static void deleteOrphanedVerticesBelow(Vertex vertex,
			HashSet<? extends Vertex> verticesToOmit,
			HashSet<Vertex> alreadyDeletedVertices) {
		if (alreadyDeletedVertices.contains(vertex)) {
			return;
		}

		HashSet<Vertex> nextOrphans = new HashSet<Vertex>();
		for (Edge inc : vertex.incidences(EdgeDirection.IN)) {
			nextOrphans.add(inc.getAlpha());
		}
		if (vertex.getFirstEdge(EdgeDirection.OUT) == null
				&& !verticesToOmit.contains(vertex)) {
			// vertex is orphaned
			alreadyDeletedVertices.add(vertex);
			// GreqlEvaluator.println("deleting " + vertex);
			vertex.delete();
			for (Vertex v : nextOrphans) {
				deleteOrphanedVerticesBelow(v, verticesToOmit,
						alreadyDeletedVertices);
			}
		}
	}

	/**
	 * Creates a new {@link GraphSize} object with default values for vertex,
	 * edge, vertex type and edge type count.
	 * 
	 * @return the created {@link GraphSize} object
	 */
	public static GraphSize getDefaultGraphSize() {
		return new GraphSize(100, 100, 20, 20);
	}
}
