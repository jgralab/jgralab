/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class Formula {
	/**
	 * The maximum number of non-constant terms a formula may have to try to
	 * calculate the optimal ordering. If there are less NCTs, then the current
	 * order will be used.
	 */
	private static final int MAX_NON_CONSTANT_TERM_NUMBER = 3;

	protected static Logger logger = JGraLab.getLogger(Formula.class
			.getPackage().getName());

	protected GreqlEvaluator greqlEvaluator;

	@Override
	public abstract String toString();

	public abstract Expression toExpression();

	public static Formula createFormulaFromExpression(Expression exp,
			GreqlEvaluator eval) {
		Formula formula = createFormulaFromExpressionInternal(eval, exp);
		OptimizerUtility.deleteOrphanedVerticesBelow(exp, new HashSet<Vertex>(
				formula.getNonConstantTermExpressions()));
		return formula;
	}

	public Formula(GreqlEvaluator eval) {
		this.greqlEvaluator = eval;
	}

	private static Formula createFormulaFromExpressionInternal(
			GreqlEvaluator eval, Expression exp) {
		assert exp.isValid() : exp + " is not valid!";
		if (exp instanceof BoolLiteral) {
			BoolLiteral bool = (BoolLiteral) exp;
			if (bool.is_boolValue()) {
				return new True(eval);
			} else {
				return new False(eval);
			}
		}

		if (exp instanceof FunctionApplication) {
			FunctionApplication funApp = (FunctionApplication) exp;
			if (OptimizerUtility.isAnd(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				Expression leftArg = (Expression) inc.getAlpha();
				Expression rightArg = (Expression) inc.getNextIsArgumentOf(
						EdgeDirection.IN).getAlpha();
				return new And(eval, createFormulaFromExpressionInternal(eval,
						leftArg), createFormulaFromExpressionInternal(eval,
						rightArg));
			}
			if (OptimizerUtility.isOr(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				Expression leftArg = (Expression) inc.getAlpha();
				Expression rightArg = (Expression) inc.getNextIsArgumentOf(
						EdgeDirection.IN).getAlpha();
				return new Or(eval, createFormulaFromExpressionInternal(eval,
						leftArg), createFormulaFromExpressionInternal(eval,
						rightArg));
			}
			if (OptimizerUtility.isNot(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				Expression arg = (Expression) inc.getAlpha();
				return new Not(eval, createFormulaFromExpressionInternal(eval,
						arg));
			}
		}

		return new NonConstantTerm(eval, exp);
	}

	public Formula optimize() {
		ArrayList<Expression> nctExpressions = getNonConstantTermExpressions();
		if (nctExpressions.size() < 2) {
			// This formula is a literal or a formula containing only one
			// non-constant term, so there's nothing to optimize.
			return this;
		}

		ConditionalExpressionUnit bestUnit = calculateBestConditionalExpressionUnit(nctExpressions);
		return bestUnit.toConditionalExpression();
	}

	/**
	 * @param nonConstantTermExpressions
	 *            A list of all expressions that are contained in any
	 *            {@link NonConstantTerm}s
	 * @return the {@link ConditionalExpressionUnit} with the highest
	 *         <code>selectivity(booleanDifference) / costs(expression)</code>
	 *         ratio
	 */
	private ConditionalExpressionUnit calculateBestConditionalExpressionUnit(
			ArrayList<Expression> nonConstantTermExpressions) {
		if (nonConstantTermExpressions.size() > MAX_NON_CONSTANT_TERM_NUMBER) {
			logger.fine("Formula: " + nonConstantTermExpressions.size()
					+ " NCTEs ==> shortcutting...");
			return new ConditionalExpressionUnit(nonConstantTermExpressions
					.get(0), this);
		}
		ConditionalExpressionUnit current, best = null;
		boolean hasTypeFunAppFound = false;
		for (Expression exp : nonConstantTermExpressions) {
			current = new ConditionalExpressionUnit(exp, this);

			// initialize best with the first one.
			if (best == null) {
				best = current;
			}

			if (containsFunApp(exp, "hasType")) {
				hasTypeFunAppFound = true;
			}
			if ((best == null)
					|| (best.getInfluenceCostRatio() < current
							.getInfluenceCostRatio())) {
				// if there was a hasType() before, attribute accesses may not
				// be pulled before! Example: hasType(v, "Foo") and v.fooAttr =
				// 19 must stay in this order.
				if (hasTypeFunAppFound && containsFunApp(exp, "getValue")) {
					continue;
				}
				best = current;
			}
		}
		return best;
	}

	/**
	 * @param exp
	 * @param functionName
	 * @return true if exp is a {@link FunctionApplication} of functionName
	 */
	private boolean isFunApp(Vertex exp, String functionName) {
		if (exp instanceof FunctionApplication) {
			FunctionApplication funApp = (FunctionApplication) exp;
			return ((FunctionId) funApp.getFirstIsFunctionIdOf().getAlpha())
					.get_name().equals(functionName);
		}
		return false;
	}

	/**
	 * @param v
	 * @param name
	 * @return true if the subgraph below v contains a
	 *         {@link FunctionApplication} of the function name
	 */
	private boolean containsFunApp(Vertex v, String name) {
		if (isFunApp(v, name)) {
			return true;
		}
		for (Edge e : v.incidences(EdgeDirection.IN)) {
			if (containsFunApp(e.getAlpha(), name)) {
				return true;
			}
		}
		return false;
	}

	protected abstract ArrayList<Expression> getNonConstantTermExpressions();

	/**
	 * Create a new {@link Formula} where each {@link NonConstantTerm} that
	 * represents the {@link Expression} <code>exp</code> is replaced by
	 * <code>literal</code>.
	 * 
	 * @param exp
	 *            the {@link Expression} whose {@link NonConstantTerm}s should
	 *            be replaced
	 * @param literal
	 *            the replacement {@link Literal}
	 * @return a new {@link Formula}
	 */
	protected abstract Formula calculateReplacementFormula(Expression exp,
			Literal literal);

	/**
	 * Create a new {@link Formula} which is simplified according these rules:
	 * <code>a and true = a</code>, <code>a and false = false</code>,
	 * <code>a or true = true</code>, <code>a or false = a</code>,
	 * <code>not true = false</code>, <code>not false = true</code>,
	 * <code>not not a = a</code>.
	 * 
	 * @return a simplified {@link Formula}
	 */
	public abstract Formula simplify();

	public abstract double getSelectivity();

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();
}
