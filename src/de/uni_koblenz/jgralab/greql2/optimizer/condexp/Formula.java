/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.TrivalentBoolean;

/**
 * TODO: (heimdall) Comment class!
 *
 * @author ist@uni-koblenz.de
 *
 */
public abstract class Formula {
	protected static Logger logger = Logger.getLogger(Formula.class
			.getPackage().getName());

	/**
	 * Indicates if a simplification or optimization (condexp-transformation)
	 * could be done.
	 */
	protected static boolean simplifiedOrOptimized = false;

	protected static Greql2 syntaxgraph;
	protected static GreqlEvaluator greqlEvaluator;

	@Override
	public abstract String toString();

	public abstract Expression toExpression();

	public static Formula createFormulaFromExpression(Expression exp) {
		syntaxgraph = (Greql2) exp.getGraph();

		Formula formula = createFormulaFromExpressionInternal(exp);

		OptimizerUtility
				.deleteOrphanedVerticesBelow(exp, new HashSet<Expression>(
						formula.getNonConstantTermExpressions()));
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
		ArrayList<Expression> nctExpressions = new ArrayList<Expression>();
		nctExpressions = getNonConstantTermExpressions();
		if (nctExpressions.size() < 2) {
			// This formula is a literal or a formula containing only one
			// non-constant term, so there's nothing to optimize.
			return this;
		}

		simplifiedOrOptimized = true;

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
			ArrayList<Expression> nonConstantTermExpressions) {
		ConditionalExpressionUnit current, best = null;
		boolean hasTypeFunAppFound = false;
		for (Expression exp : nonConstantTermExpressions) {
			current = new ConditionalExpressionUnit(exp, this);
			if (containsFunApp(exp, "hasType")) {
				hasTypeFunAppFound = true;
			}
			if (best == null
					|| best.getInfluenceCostRatio() < current
							.getInfluenceCostRatio()) {
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
					.getName().equals(functionName);
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

	public static void setGreqlEvaluator(GreqlEvaluator greqlEvaluator) {
		Formula.greqlEvaluator = greqlEvaluator;
	}

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();

	public static boolean isSimplifiedOrOptimized() {
		return simplifiedOrOptimized;
	}

	public static void setSimplifiedOrOptimized(boolean simplifiedOrOptimized) {
		Formula.simplifiedOrOptimized = simplifiedOrOptimized;
	}
}
