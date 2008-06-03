/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import java.util.HashSet;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.TrivalentBoolean;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public abstract class Formula {

	protected final static boolean DEBUG = false;

	protected static Greql2 syntaxgraph;
	protected static GreqlEvaluator greqlEvaluator;

	@Override
	public abstract String toString();

	public abstract Expression toExpression();

	public static Formula createFormulaFromExpression(Expression exp) {
		syntaxgraph = (Greql2) exp.getGraph();

		Formula formula = createFormulaFromExpressionInternal(exp);

		OptimizerUtility.deleteOrphanedVerticesBelow(exp, formula
				.getNonConstantTermExpressions());
		return formula;
	}

	private static Formula createFormulaFromExpressionInternal(Expression exp) {
		if (exp instanceof BoolLiteral) {
			BoolLiteral bool = (BoolLiteral) exp;
			TrivalentBoolean value = bool.getBoolValue();
			if (value == TrivalentBoolean.TRUE) {
				return new True();
			}
			if (value == TrivalentBoolean.FALSE) {
				return new False();
			}
			return new Null();
		}

		if (exp instanceof FunctionApplication) {
			FunctionApplication funApp = (FunctionApplication) exp;
			if (OptimizerUtility.isAnd(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				Expression leftArg = (Expression) inc.getAlpha();
				Expression rightArg = (Expression) inc.getNextIsArgumentOf(
						EdgeDirection.IN).getAlpha();
				return new And(createFormulaFromExpressionInternal(leftArg),
						createFormulaFromExpressionInternal(rightArg));
			}
			if (OptimizerUtility.isOr(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				Expression leftArg = (Expression) inc.getAlpha();
				Expression rightArg = (Expression) inc.getNextIsArgumentOf(
						EdgeDirection.IN).getAlpha();
				return new Or(createFormulaFromExpressionInternal(leftArg),
						createFormulaFromExpressionInternal(rightArg));
			}
			if (OptimizerUtility.isNot(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				Expression arg = (Expression) inc.getAlpha();
				return new Not(createFormulaFromExpressionInternal(arg));
			}
		}

		return new NonConstantTerm(exp);
	}

	public Formula optimize() {
		HashSet<Expression> nctExpressions = getNonConstantTermExpressions();
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
	 *            A set of all expressions that are contained in any
	 *            {@link NonConstantTerm}s
	 * @return the {@link ConditionalExpressionUnit} with the highest
	 *         <code>selectivity(booleanDifference) / costs(expression)</code>
	 *         ratio
	 */
	private ConditionalExpressionUnit calculateBestConditionalExpressionUnit(
			HashSet<Expression> nonConstantTermExpressions) {
		ConditionalExpressionUnit current, best = null;
		for (Expression exp : nonConstantTermExpressions) {
			current = new ConditionalExpressionUnit(exp, this);
			if (best == null
					|| best.getInfluenceCostRatio() < current
							.getInfluenceCostRatio())
				best = current;
		}
		return best;
	}

	protected abstract HashSet<Expression> getNonConstantTermExpressions();

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

	public static void setGreqlEvaluator(GreqlEvaluator greqlEvaluator) {
		Formula.greqlEvaluator = greqlEvaluator;
	}

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();
}
