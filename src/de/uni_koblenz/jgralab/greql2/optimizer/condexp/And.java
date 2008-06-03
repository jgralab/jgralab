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
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
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

		if (lhs instanceof False)
			return lhs;

		if (rhs instanceof False)
			return rhs;

		if (lhs instanceof True)
			return rhs;

		if (rhs instanceof True)
			return lhs;

		if (lhs.equals(rhs))
			return lhs;

		return new And(lhs, rhs);
	}

	@Override
	public double getSelectivity() {
		double selectivity = leftHandSide.getSelectivity()
				* rightHandSide.getSelectivity();
		if (DEBUG)
			System.out.println("selectivity[" + this + "] = " + selectivity);
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