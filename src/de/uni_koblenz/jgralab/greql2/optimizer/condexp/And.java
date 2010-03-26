/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class And extends BinaryOperator {

	public And(GreqlEvaluator eval, Formula lhs, Formula rhs) {
		super(eval, lhs, rhs);
	}

	@Override
	public String toString() {
		return "(" + leftHandSide + " & " + rightHandSide + ")";
	}

	@Override
	public Expression toExpression() {
		Greql2 syntaxgraph = greqlEvaluator.getSyntaxGraph();
		FunctionApplication funApp = syntaxgraph.createFunctionApplication();
		FunctionId funId = OptimizerUtility.findOrCreateFunctionId("and",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(funId, funApp);
		syntaxgraph.createIsArgumentOf(leftHandSide.toExpression(), funApp);
		syntaxgraph.createIsArgumentOf(rightHandSide.toExpression(), funApp);
		return funApp;
	}

	@Override
	protected Formula calculateReplacementFormula(Expression exp,
			Literal literal) {
		return new And(greqlEvaluator, leftHandSide
				.calculateReplacementFormula(exp, literal), rightHandSide
				.calculateReplacementFormula(exp, literal));
	}

	@Override
	public Formula simplify() {
		Formula lhs = leftHandSide.simplify();
		Formula rhs = rightHandSide.simplify();

		// BEWARE: (x & ~x) is NOT always false in GReQL, cause it's null, if x
		// evaluates to null...

		if (lhs.equals(new Not(greqlEvaluator, rhs))
				|| new Not(greqlEvaluator, lhs).equals(rhs)) {
			return new False(greqlEvaluator);
		}

		if (lhs instanceof False) {
			return lhs;
		}

		if (rhs instanceof False) {
			return rhs;
		}

		if (lhs instanceof True) {
			return rhs;
		}

		if (rhs instanceof True) {
			return lhs;
		}

		if ((lhs instanceof Null) && isOrWithNullLeaf(rhs)) {
			return lhs;
		}

		if ((rhs instanceof Null) && isOrWithNullLeaf(lhs)) {
			return rhs;
		}

		if ((lhs instanceof Null) && isAndWithNullLeaf(rhs)) {
			return rhs;
		}

		if ((rhs instanceof Null) && isAndWithNullLeaf(lhs)) {
			return lhs;
		}

		if (lhs.equals(rhs)) {
			return lhs;
		}

		return new And(greqlEvaluator, lhs, rhs);
	}

	@Override
	public double getSelectivity() {
		double selectivity = leftHandSide.getSelectivity()
				* rightHandSide.getSelectivity();
		logger.finer("selectivity[" + this + "] = " + selectivity);
		return selectivity;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof And) {
			And and = (And) o;
			return leftHandSide.equals(and.leftHandSide)
					&& rightHandSide.equals(and.rightHandSide);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode(17);
	}
}