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
public class Or extends BinaryOperator {

	public Or(GreqlEvaluator eval, Formula lhs, Formula rhs) {
		super(eval, lhs, rhs);
	}

	@Override
	public String toString() {
		return "(" + leftHandSide + " | " + rightHandSide + ")";
	}

	@Override
	public Expression toExpression() {
		Greql2 syntaxgraph = greqlEvaluator.getSyntaxGraph();
		FunctionApplication funApp = syntaxgraph.createFunctionApplication();
		FunctionId funId = OptimizerUtility.findOrCreateFunctionId("or",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(funId, funApp);
		syntaxgraph.createIsArgumentOf(leftHandSide.toExpression(), funApp);
		syntaxgraph.createIsArgumentOf(rightHandSide.toExpression(), funApp);
		return funApp;
	}

	@Override
	protected Formula calculateReplacementFormula(Expression exp,
			Literal literal) {
		return new Or(greqlEvaluator, leftHandSide.calculateReplacementFormula(
				exp, literal), rightHandSide.calculateReplacementFormula(exp,
				literal));
	}

	@Override
	public Formula simplify() {
		Formula lhs = leftHandSide.simplify();
		Formula rhs = rightHandSide.simplify();

		// BEWARE: (x | ~x) is NOT always true in GReQL, cause it's null, if x
		// evaluates to null...

		if (lhs instanceof True) {
			return lhs;
		}

		if (rhs instanceof True) {
			return rhs;
		}

		if (lhs instanceof False) {
			return rhs;
		}

		if (rhs instanceof False) {
			return lhs;
		}

		if (lhs.equals(rhs)) {
			return lhs;
		}

		return new Or(greqlEvaluator, lhs, rhs);
	}

	@Override
	public double getSelectivity() {
		double selectivity = 1 - (1 - leftHandSide.getSelectivity())
				* (1 - rightHandSide.getSelectivity());
		logger.finer("selectivity[" + this + "] = " + selectivity);
		return selectivity;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Or) {
			Or or = (Or) o;
			return leftHandSide.equals(or.leftHandSide)
					&& rightHandSide.equals(or.rightHandSide);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode(19);
	}
}
