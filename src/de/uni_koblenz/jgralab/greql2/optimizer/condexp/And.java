/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class And extends BinaryOperator {

	public And(Formula lhs, Formula rhs) {
		super(lhs, rhs);
	}

	@Override
	public String toString() {
		return "(" + leftHandSide + " & " + rightHandSide + ")";
	}

	@Override
	public Expression toExpression() {
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
		return new And(leftHandSide.calculateReplacementFormula(exp, literal),
				rightHandSide.calculateReplacementFormula(exp, literal));
	}

	@Override
	public Formula simplify() {
		Formula lhs = leftHandSide.simplify();
		Formula rhs = rightHandSide.simplify();

		if (lhs instanceof False) {
			simplifiedOrOptimized = true;
			return lhs;
		}

		if (rhs instanceof False) {
			simplifiedOrOptimized = true;
			return rhs;
		}

		if (lhs instanceof True) {
			simplifiedOrOptimized = true;
			return rhs;
		}

		if (rhs instanceof True) {
			simplifiedOrOptimized = true;
			return lhs;
		}

		if (lhs instanceof Null && isOrWithNullLeaf(rhs)) {
			simplifiedOrOptimized = true;
			return lhs;
		}

		if (rhs instanceof Null && isOrWithNullLeaf(lhs)) {
			simplifiedOrOptimized = true;
			return rhs;
		}

		if (lhs instanceof Null && isAndWithNullLeaf(rhs)) {
			simplifiedOrOptimized = true;
			return rhs;
		}

		if (rhs instanceof Null && isAndWithNullLeaf(lhs)) {
			simplifiedOrOptimized = true;
			return lhs;
		}

		if (lhs.equals(rhs)) {
			simplifiedOrOptimized = true;
			return lhs;
		}

		return new And(lhs, rhs);
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