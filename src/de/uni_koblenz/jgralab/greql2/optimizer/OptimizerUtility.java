/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.funlib.And;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.Identifier;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * Holds various static methods used by {@link Optimizer}s.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class OptimizerUtility {

	private static Logger logger = Logger.getLogger(OptimizerUtility.class
			.getName());

	/**
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
		Edge inc = v1.getFirstEdge(EdgeDirection.IN);
		while (inc != null) {
			if (isAbove(inc.getAlpha(), v2)) {
				return true;
			}
			inc = inc.getNextEdge(EdgeDirection.IN);
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
	 * Check if <code>var1</code> is declared before <code>var2</code>. A
	 * {@link Variable} is declared before another variable, if it's declared in
	 * an outer {@link Declaration}, or if it's declared in the same
	 * {@link Declaration} but in a {@link SimpleDeclaration} that comes before
	 * the other {@link Variable}'s {@link SimpleDeclaration}, or if it's
	 * declared in the same {@link SimpleDeclaration} but is connected to that
	 * earlier (meaning its {@link IsDeclaredVarOf} edge comes before the
	 * other's).
	 * 
	 * Note that a {@link Variable} is never declared before itself.
	 * 
	 * @param var1
	 *            a {@link Variable}
	 * @param var2
	 *            a {@link Variable}
	 * @return <code>true</code> if <code>var1</code> is declared before
	 *         <code>var2</code>, <code>false</code> otherwise.
	 */
	public static boolean isDeclaredBefore(Variable var1, Variable var2) {
		// GreqlEvaluator.println("isDeclaredBefore(" + var1 + ", " + var2 +
		// ")");
		if (var1 == var2) {
			return false;
		}

		SimpleDeclaration sd1 = (SimpleDeclaration) var1
				.getFirstIsDeclaredVarOf(EdgeDirection.OUT).getOmega();
		Declaration decl1 = (Declaration) sd1.getFirstIsSimpleDeclOf(
				EdgeDirection.OUT).getOmega();
		SimpleDeclaration sd2 = (SimpleDeclaration) var2
				.getFirstIsDeclaredVarOf(EdgeDirection.OUT).getOmega();
		Declaration decl2 = (Declaration) sd2.getFirstIsSimpleDeclOf(
				EdgeDirection.OUT).getOmega();

		if (decl1 == decl2) {
			if (sd1 == sd2) {
				// var1 and var2 are declared in the same SimpleDeclaration,
				// so the order of the IsDeclaredVarOf edges matters.
				IsDeclaredVarOf inc = sd1
						.getFirstIsDeclaredVarOf(EdgeDirection.IN);
				while (inc != null) {
					if (inc.getAlpha() == var1) {
						return true;
					}
					if (inc.getAlpha() == var2) {
						return false;
					}
					inc = inc.getNextIsDeclaredVarOf(EdgeDirection.IN);
				}
			} else {
				// var1 and var2 are declared in the same Declaration but
				// different SimpleDeclarations, so the order of the
				// SimpleDeclarations matters.
				IsSimpleDeclOf inc = decl1
						.getFirstIsSimpleDeclOf(EdgeDirection.IN);
				while (inc != null) {
					if (inc.getAlpha() == sd1) {
						return true;
					}
					if (inc.getAlpha() == sd2) {
						return false;
					}
					inc = inc.getNextIsSimpleDeclOf(EdgeDirection.IN);
				}
			}
		} else {
			// start and target are declared in different Declarations, so we
			// have to check if start was declared in the outer Declaration.
			Vertex declParent1 = decl1.getFirstEdge(EdgeDirection.OUT)
					.getOmega();
			Vertex declParent2 = decl2.getFirstEdge(EdgeDirection.OUT)
					.getOmega();
			if (isAbove(declParent1, declParent2)) {
				return true;
			} else {
				return false;
			}
		}
		logger
				.severe("No case matched in isDeclaredBefore(Variable, Variable)."
						+ " That must not happen!");
		return false;
	}

	/**
	 * Makes a deep copy of the subgraph given by <code>origVertex</code>. For
	 * each {@link Vertex} in that subgraph a new {@link Vertex} of the same
	 * type will be created, likewise for the {@link Edge}s. As an exception to
	 * that rule, {@link Identifier}s other than {@link Variable}s won't be
	 * copied. For {@link Variable}s it's quite complicated. If a
	 * {@link Variable} is in <code>variablesToBeCopied</code> it will be copied
	 * ONCE. After that the one and only copy is used instead of creating a new
	 * copy. That's what <code>copiedVarMap</code> is for. So normally you'd
	 * provide an empty {@link HashMap}.
	 * 
	 * @param origVertex
	 *            the root {@link Vertex} of the subgraph to be copied
	 * @param graph
	 *            the {@link Graph} where <code>origVertex</code> is part of
	 * @param variablesToBeCopied
	 *            a set of {@link Variable}s that should be copied ONCE
	 * @param copiedVarMap
	 *            a {@link HashMap} form the original {@link Variable} to its
	 *            one and only copy
	 * @return the root {@link Vertex} of the copy
	 */
	@SuppressWarnings("unchecked")
	public static Vertex copySubgraph(Vertex origVertex, Greql2 graph,
			Set<Variable> variablesToBeCopied,
			HashMap<Variable, Variable> copiedVarMap) {
		// GreqlEvaluator.println("copySubgraph(" + origVertex + ", graph, "
		// + variablesToBeCopied + ", " + copiedVarMap + ")");
		if (origVertex instanceof Identifier
				&& !(origVertex instanceof Variable)) {
			return origVertex;
		}
		if (origVertex instanceof Variable) {
			if (copiedVarMap.containsKey(origVertex)) {
				return copiedVarMap.get(origVertex);
			}
			if (!variablesToBeCopied.contains(origVertex)) {
				return origVertex;
			}
		}

		Class<? extends Vertex> vertexClass = (Class<? extends Vertex>) origVertex
				.getAttributedElementClass().getM1Class();
		Vertex topVertex = graph.createVertex(vertexClass);
		copyAttributes(origVertex, topVertex);

		if (topVertex instanceof Variable) {
			copiedVarMap.put((Variable) origVertex, (Variable) topVertex);
		}

		Edge origEdge = origVertex.getFirstEdge(EdgeDirection.IN);
		Vertex subVertex;

		while (origEdge != null) {
			subVertex = copySubgraph(origEdge.getAlpha(), graph,
					variablesToBeCopied, copiedVarMap);
			Class<? extends Edge> edgeClass = (Class<? extends Edge>) origEdge
					.getAttributedElementClass().getM1Class();
			graph.createEdge(edgeClass, subVertex, topVertex);
			origEdge = origEdge.getNextEdge(EdgeDirection.IN);
		}

		return topVertex;
	}

	/**
	 * Copy the attribute values of <code>from</code> to <code>to</code>. The
	 * types of the given {@link AttributedElement}s have to be equal.
	 * 
	 * @param from
	 *            an {@link AttributedElement}
	 * @param to
	 *            another {@link AttributedElement} whose runtime type equals
	 *            <code>from</code>'s type.
	 */
	public static void copyAttributes(AttributedElement from,
			AttributedElement to) {
		for (Attribute attr : from.getAttributedElementClass()
				.getAttributeList()) {
			try {
				to.setAttribute(attr.getName(), from.getAttribute(attr
						.getName()));
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
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
		IsDeclaredVarOf inc = sd.getFirstIsDeclaredVarOf(EdgeDirection.IN);
		while (inc != null) {
			vars.add((Variable) inc.getAlpha());
			inc = inc.getNextIsDeclaredVarOf(EdgeDirection.IN);
		}
		return vars;
	}

	/**
	 * Collect all of {@link Variable}s that are located below <code>v</code>.
	 * 
	 * @param vertex
	 *            the root {@link Vertex} below which to look for
	 *            {@link Variable}s
	 * @return a {@link Set} of {@link Variable}s that are located below
	 *         <code>v</code>
	 */
	public static Set<Variable> collectVariablesBelow(Vertex vertex) {
		return collectVariablesBelow(vertex, new HashSet<Variable>());
	}

	private static Set<Variable> collectVariablesBelow(Vertex vertex,
			Set<Variable> vars) {
		// GreqlEvaluator.println("collectVariablesBelow(" + vertex + ")");
		if (vertex instanceof Variable) {
			vars.add((Variable) vertex);
			return vars;
		}
		Edge inc = vertex.getFirstEdge(EdgeDirection.IN);
		while (inc != null) {
			// GreqlEvaluator.println(inc + " <-- " + inc.getAlpha());
			collectVariablesBelow(inc.getAlpha(), vars);
			inc = inc.getNextEdge(EdgeDirection.IN);
		}
		return vars;
	}

	/**
	 * Collect all {@link SimpleDeclaration}s of <code>decl</code> in a
	 * {@link List}.
	 * 
	 * @param decl
	 *            a {@link Declaration}
	 * @return a {@link List} of all {@link SimpleDeclaration}s that are part of
	 *         <code>decl</code>
	 */
	public static List<SimpleDeclaration> collectSimpleDeclarationsOf(
			Declaration decl) {
		ArrayList<SimpleDeclaration> sds = new ArrayList<SimpleDeclaration>();
		IsSimpleDeclOf inc = decl.getFirstIsSimpleDeclOf(EdgeDirection.IN);
		while (inc != null) {
			sds.add((SimpleDeclaration) inc.getAlpha());
			inc = inc.getNextIsSimpleDeclOf(EdgeDirection.IN);
		}
		return sds;
	}

	/**
	 * Collect all {@link Variable}s declared by the {@link SimpleDeclaration}s
	 * of <code>decl</code>.
	 * 
	 * @param decl
	 *            a {@link Declaration}
	 * @return a {@link Set} of all {@link Variable}s declared by the
	 *         {@link SimpleDeclaration}s of <code>decl</code>.
	 */
	public static Set<Variable> collectVariablesDeclaredBy(Declaration decl) {
		HashSet<Variable> vars = new HashSet<Variable>();
		IsSimpleDeclOf inc = decl.getFirstIsSimpleDeclOf(EdgeDirection.IN);
		while (inc != null) {
			vars.addAll(collectVariablesDeclaredBy((SimpleDeclaration) inc
					.getAlpha()));
			inc = inc.getNextIsSimpleDeclOf(EdgeDirection.IN);
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
		if (alreadyDeletedVertices.contains(vertex))
			return;

		HashSet<Vertex> nextOrphans = new HashSet<Vertex>();
		Edge inc = vertex.getFirstEdge(EdgeDirection.IN);
		while (inc != null) {
			nextOrphans.add(inc.getAlpha());
			inc = inc.getNextEdge(EdgeDirection.IN);
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

	@SuppressWarnings("unchecked")
	public static Vertex copySubgraph(Vertex vertex,
			HashSet<? extends Vertex> verticesNotToBeCopied, Graph graph) {
		if (verticesNotToBeCopied.contains(vertex))
			return vertex;

		Class<? extends Vertex> class1 = (Class<? extends Vertex>) vertex
				.getAttributedElementClass().getM1Class();
		Class<? extends Vertex> vertexClass = class1;
		Vertex vcopy = graph.createVertex(vertexClass);
		Edge edge = vertex.getFirstEdge(EdgeDirection.IN);
		while (edge != null) {
			Class<? extends Edge> edgeClass = (Class<? extends Edge>) edge
					.getAttributedElementClass().getM1Class();
			graph.createEdge(edgeClass, copySubgraph(edge.getAlpha(),
					verticesNotToBeCopied, graph), vcopy);
			edge = edge.getNextEdge(EdgeDirection.IN);
		}
		return vcopy;
	}

	public static Expression createConjunction(
			List<IsConstraintOf> constraintEdges, Greql2 syntaxgraph) {
		if (constraintEdges.size() == 1) {
			return (Expression) constraintEdges.get(0).getAlpha();
		}
		FunctionApplication funApp = syntaxgraph.createFunctionApplication();
		FunctionId funId = OptimizerUtility.findOrCreateFunctionId("and",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(funId, funApp);
		syntaxgraph.createIsArgumentOf((Expression) constraintEdges.get(0)
				.getAlpha(), funApp);
		syntaxgraph.createIsArgumentOf(createConjunction(constraintEdges
				.subList(1, constraintEdges.size()), syntaxgraph), funApp);
		return funApp;
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

	/**
	 * @param edge
	 *            the start {@link Edge}
	 * @param target
	 *            the target {@link Vertex}
	 * @return <code>true</code> if there's a forward directed path from
	 *         <code>edge</code> to <code>target</code> with no other vertices
	 *         of <code>target</code>'s class in between, <code>false</code>
	 *         otherwise
	 */
	public static boolean existsForwardPathExcludingOtherTargetClassVertices(
			Edge edge, Vertex target) {
		// GreqlEvaluator.println("iFPT(" + edge + ", " + target + ");");
		Vertex omega = edge.getOmega();

		if (omega == target)
			return true;

		if (omega.getAttributedElementClass().getM1Class() == target
				.getAttributedElementClass().getM1Class())
			return false;

		Edge e = omega.getFirstEdge(EdgeDirection.OUT);
		while (e != null) {
			if (existsForwardPathExcludingOtherTargetClassVertices(e, target))
				return true;
			e = e.getNextEdge(EdgeDirection.OUT);
		}
		return false;
	}
}
