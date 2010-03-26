/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * Base class for all {@link Optimizer}s which defines some useful methods that
 * are needed in derived Classes.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class OptimizerBase implements Optimizer {

	protected String optimizerHeaderString() {
		return "*** " + this.getClass().getSimpleName() + ": ";
	}

	protected void recreateVertexEvaluators(GreqlEvaluator eval) {
		try {
			eval.createVertexEvaluators();
		} catch (EvaluateException e) {
			e.printStackTrace();
			throw new OptimizerException(
					"Exception while re-creating VertexEvaluators.", e);
		}
	}

	/**
	 * Put all edges going in or coming out of vertex <code>from</code> to
	 * vertex <code>to</code>. If there's already an edge of exactly that type
	 * between <code>from</code>'s that-vertex and <code>to</code>, then don't
	 * create a duplicate edge, unless <code>allowDuplicateEdges</code> is true.
	 * 
	 * @param from
	 *            the old vertex
	 * @param to
	 *            the new vertex
	 * @param allowDuplicateEdges
	 */
	protected void relink(Vertex from, Vertex to) {
		assert (from != null) && (to != null) : "Relinking null!";
		assert from != to : "Relinking from itself!";
		assert from.getM1Class() == to.getM1Class() : "Relinking different classes! from is "
				+ from + ", to is " + to;
		assert from.isValid() && to.isValid() : "Relinking invalid vertices!";

		// System.out.println("    relink: " + from + " --> " + to);
		Edge e = from.getFirstEdge(EdgeDirection.IN);
		while (e != null) {
			Edge newE = e.getNextEdge(EdgeDirection.IN);
			e.setOmega(to);
			e = newE;
		}
		e = from.getFirstEdge(EdgeDirection.OUT);
		while (e != null) {
			Edge newE = e.getNextEdge(EdgeDirection.OUT);
			e.setAlpha(to);
			e = newE;
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
	protected boolean isDeclaredBefore(Variable var1, Variable var2) {
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
			if (OptimizerUtility.isAbove(declParent1, declParent2)) {
				return true;
			} else {
				return false;
			}
		}
		throw new OptimizerException(
				"No case matched in isDeclaredBefore(Variable, Variable)."
						+ " That must not happen!");
	}

	/**
	 * Find the nearest {@link Declaration} above <code>vertex</code>.
	 * 
	 * @param vertex
	 *            a {@link Vertex}
	 * @return nearest {@link Declaration} above <code>vertex</code>
	 */
	protected Declaration findNearestDeclarationAbove(Vertex vertex) {
		if (vertex instanceof Declaration) {
			return (Declaration) vertex;
		}
		Declaration result = null;
		Edge inc = vertex.getFirstEdge(EdgeDirection.OUT);
		while (inc != null) {
			result = findNearestDeclarationAbove(inc.getOmega());
			if (result != null) {
				return result;
			}
			inc = inc.getNextEdge(EdgeDirection.OUT);
		}
		return null;
	}

	/**
	 * Split the given {@link SimpleDeclaration} so that there's one
	 * {@link SimpleDeclaration} that declares the {@link Variable}s in
	 * <code>varsToBeSplit</code> and one for the rest.
	 * 
	 * @param sd
	 *            the {@link SimpleDeclaration} to be split
	 * @param varsToBeSplit
	 *            a {@link Set} of {@link Variable}s that should have their own
	 *            {@link SimpleDeclaration}
	 * @return the newly created {@link SimpleDeclaration} declaring all
	 *         <code>varsToBeSplit</code>
	 */
	protected SimpleDeclaration splitSimpleDeclaration(SimpleDeclaration sd,
			Set<Variable> varsToBeSplit) {
		Greql2 syntaxgraph = (Greql2) sd.getGraph();
		Set<Variable> varsDeclaredBySD = OptimizerUtility
				.collectVariablesDeclaredBy(sd);

		if (varsDeclaredBySD.size() == varsToBeSplit.size()) {
			// there's nothing to split out anymore
			return sd;
		}
		Declaration parentDecl = (Declaration) sd.getFirstIsSimpleDeclOf(
				EdgeDirection.OUT).getOmega();
		IsSimpleDeclOf oldEdge = sd.getFirstIsSimpleDeclOf();
		SimpleDeclaration newSD = syntaxgraph.createSimpleDeclaration();
		IsSimpleDeclOf newEdge = syntaxgraph.createIsSimpleDeclOf(newSD,
				parentDecl);
		syntaxgraph.createIsTypeExprOfDeclaration((Expression) sd
				.getFirstIsTypeExprOfDeclaration(EdgeDirection.IN).getAlpha(),
				newSD);
		newEdge.getReversedEdge().putEdgeAfter(oldEdge.getReversedEdge());

		for (Variable var : varsToBeSplit) {
			IsDeclaredVarOf inc = sd.getFirstIsDeclaredVarOf(EdgeDirection.IN);
			HashSet<IsDeclaredVarOf> relinkIncs = new HashSet<IsDeclaredVarOf>();
			while (inc != null) {
				if (inc.getAlpha() == var) {
					// This inc is now declared by newSD, so we need to relink
					// the edge.
					relinkIncs.add(inc);
				}
				inc = inc.getNextIsDeclaredVarOf(EdgeDirection.IN);
			}
			for (IsDeclaredVarOf relinkEdge : relinkIncs) {
				relinkEdge.setOmega(newSD);
			}
		}
		return newSD;
	}

}
