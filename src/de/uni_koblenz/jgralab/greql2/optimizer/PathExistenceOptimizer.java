/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.funlib.Contains;
import de.uni_koblenz.jgralab.greql2.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.PathExistence;
import de.uni_koblenz.jgralab.greql2.schema.PathExpression;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * This {@link Optimizer} transforms {@link PathExistence} vertices to
 * {@link FunctionApplication}s of the {@link Contains} function applied to a
 * {@link PathExpression}.
 *
 * @author ist@uni-koblenz.de
 *
 */
public class PathExistenceOptimizer extends OptimizerBase {

	private static Logger logger = Logger
			.getLogger(PathExistenceOptimizer.class.getName());

	private Greql2 syntaxgraph;

	private boolean anOptimizationWasDone = false;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz
	 * .jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof PathExistenceOptimizer) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz
	 * .jgralab.greql2.evaluator.GreqlEvaluator,
	 * de.uni_koblenz.jgralab.greql2.schema.Greql2)
	 */
	@Override
	public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
			throws OptimizerException {
		anOptimizationWasDone = false;
		this.syntaxgraph = syntaxgraph;

		runOptimization();

		try {
			eval.createVertexEvaluators();
		} catch (EvaluateException e) {
			e.printStackTrace();
		}

		OptimizerUtility.createMissingSourcePositions(syntaxgraph);

		return anOptimizationWasDone;
	}

	/**
	 * Iterate through all {@link PathExistence} vertices and apply the
	 * transformation if it is remunerative.
	 */
	private void runOptimization() {
		Set<PathExistence> pathExistenceVertices = collectPathExistenceVertices();
		for (PathExistence pe : pathExistenceVertices) {
			maybeTransformPathExistence(pe);
		}
	}

	/**
	 * Check if it's worth to replace the given {@link PathExistence}
	 * <code>pe</code> with a {@link FunctionApplication} of the
	 * {@link Contains} function combined with a {@link PathExpression}.
	 *
	 * Such a transformation remunerates if the {@link PathExpression} isn't
	 * evaluated as often as the {@link PathExistence} is. Here's an example:
	 *
	 * The {@link PathExistence} <code>a --&gt; b</code> has to be evaluated
	 * whenever <code>a</code> or <code>b</code> change their value. If
	 * <code>a</code> is declared before <code>b</code>, then
	 * <code>contains(a --&gt;, b)</code> is faster, because the costly
	 * {@link ForwardVertexSet} <code>a --&gt;</code> has only to be evaluated
	 * when <code>a</code> changes its value. In that case that happens only all
	 * <code>|b|</code> steps.
	 *
	 * If <code>b</code> is declared before <code>a</code>, then
	 * <code>contains(--&gt; b, a)</code> is faster, because the costly
	 * {@link BackwardVertexSet} <code>--&gt; b</code> has only to be evaluated
	 * when <code>b</code> changes its value. In that case that happens only all
	 * <code>|a|</code> steps.
	 *
	 * @param pe
	 *            a {@link PathExistence} vertex
	 */
	private void maybeTransformPathExistence(PathExistence pe) {
		Expression startExp = (Expression) pe.getFirstIsStartExprOf(
				EdgeDirection.IN).getAlpha();
		Expression targetExp = (Expression) pe.getFirstIsTargetExprOf(
				EdgeDirection.IN).getAlpha();

		Comparator<Variable> comparator = new Comparator<Variable>() {
			@Override
			public int compare(Variable v1, Variable v2) {
				if (v1 == v2) {
					return 0;
				}
				if (isDeclaredBefore(v1, v2)) {
					return -1;
				}
				return 1;
			}
		};
		TreeSet<Variable> startExpVars = new TreeSet<Variable>(comparator);
		startExpVars.addAll(OptimizerUtility.collectVariablesBelow(startExp));
		TreeSet<Variable> targetExpVars = new TreeSet<Variable>(comparator);
		targetExpVars.addAll(OptimizerUtility.collectVariablesBelow(targetExp));

		if (startExpVars.isEmpty() && targetExpVars.isEmpty()) {
			return;
		}

		if (startExpVars.isEmpty()
				|| (!targetExpVars.isEmpty() && isDeclaredBefore(startExpVars
						.last(), targetExpVars.last()))) {
			replacePathExistenceWithContainsFunApp(pe, startExp, targetExp,
					true);
		} else if (targetExpVars.isEmpty()
				|| (!startExpVars.isEmpty() && isDeclaredBefore(targetExpVars
						.last(), startExpVars.last()))) {
			replacePathExistenceWithContainsFunApp(pe, targetExp, startExp,
					false);
		}
	}

	/**
	 * Replace the given {@link PathExistence} vertex <code>pe</code> with a
	 * {@link FunctionApplication} of the {@link Contains} function.
	 *
	 * @param pe
	 *            the {@link PathExistence} to replace
	 * @param startOrTargetExp
	 *            the {@link Expression} that should be used as start expression
	 *            of the {@link ForwardVertexSet} if <code>forward</code> is
	 *            <code>true</code>, or the {@link Expression} that should be
	 *            used as target expression of the {@link BackwardVertexSet} if
	 *            <code>forward</code> is <code>false</code>.
	 * @param otherExp
	 *            the {@link Expression} that should be used second argument to
	 *            the {@link Contains} {@link FunctionApplication}
	 * @param forward
	 *            If <code>true</code> a {@link ForwardVertexSet} will be
	 *            attached to the {@link Contains} {@link FunctionApplication}
	 *            and <code>startOrTargetExp</code> will be the
	 *            {@link ForwardVertexSet}'s start expression. If
	 *            <code>false</code> a {@link BackwardVertexSet} will be
	 *            attached to the {@link Contains} {@link FunctionApplication}
	 *            and <code>startOrTargetExp</code> will be the
	 *            {@link BackwardVertexSet}'s target expression and
	 *            <code>targetOrStartExp</code>.
	 */
	private void replacePathExistenceWithContainsFunApp(PathExistence pe,
			Expression startOrTargetExp, Expression otherExp, boolean forward) {
		logger.info(optimizerHeaderString() + "Replacing " + pe
				+ " with a contains FunctionApplication using a "
				+ ((forward) ? "Forward" : "Backward") + "VertexSet.");

		anOptimizationWasDone = true;

		Edge inc = pe.getFirstEdge(EdgeDirection.OUT);
		Set<Edge> edgesToRelink = new HashSet<Edge>();
		while (inc != null) {
			edgesToRelink.add(inc);
			inc = inc.getNextEdge(EdgeDirection.OUT);
		}
		FunctionApplication contains = syntaxgraph.createFunctionApplication();
		FunctionId containsId = OptimizerUtility.findOrCreateFunctionId(
				"contains", syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(containsId, contains);
		PathExpression vertexSet;
		if (forward) {
			vertexSet = syntaxgraph.createForwardVertexSet();
			syntaxgraph.createIsStartExprOf(startOrTargetExp, vertexSet);
		} else {
			vertexSet = syntaxgraph.createBackwardVertexSet();
			syntaxgraph.createIsTargetExprOf(startOrTargetExp, vertexSet);
		}
		syntaxgraph.createIsPathOf((Expression) pe.getFirstIsPathOf(
				EdgeDirection.IN).getAlpha(), vertexSet);

		syntaxgraph.createIsArgumentOf(vertexSet, contains);
		syntaxgraph.createIsArgumentOf(otherExp, contains);
		for (Edge edge : edgesToRelink) {
			edge.setAlpha(contains);
		}
		pe.delete();
	}

	/**
	 * Collect all {@link PathExistence} vertices in the current {@link Greql2}
	 * graph.
	 *
	 * @return a {@link Set} of all {@link PathExistence} vertices of the
	 *         current {@link Greql2} graph.
	 */
	private Set<PathExistence> collectPathExistenceVertices() {
		HashSet<PathExistence> pathExistenceVertices = new HashSet<PathExistence>();
		for (PathExistence pe : syntaxgraph.getPathExistenceVertices()) {
			pathExistenceVertices.add(pe);
		}
		return pathExistenceVertices;
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
	private boolean isDeclaredBefore(Variable var1, Variable var2) {
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
		logger
				.severe("No case matched in isDeclaredBefore(Variable, Variable)."
						+ " That must not happen!");
		return false;
	}

}
