/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import java.util.ArrayList;

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
public class Not extends Formula {

	protected Formula formula;

	public Not(Formula formula) {
		this.formula = formula;
	}

	@Override
	public String toString() {
		return "~" + formula;
	}

	@Override
	public Expression toExpression() {
		FunctionApplication funApp = syntaxgraph.createFunctionApplication();
		FunctionId funId = OptimizerUtility.findOrCreateFunctionId("not",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(funId, funApp);
		syntaxgraph.createIsArgumentOf(formula.toExpression(), funApp);
		return funApp;
	}

	@Override
	protected ArrayList<Expression> getNonConstantTermExpressions() {
		return formula.getNonConstantTermExpressions();
	}

	@Override
	protected Formula calculateReplacementFormula(Expression exp,
			Literal literal) {
		return new Not(formula.calculateReplacementFormula(exp, literal));
	}

	@Override
	public Formula simplify() {
		Formula f = formula.simplify();

		if (f instanceof True) {
			simplifiedOrOptimized = true;
			return new False();
		}

		if (f instanceof False) {
			simplifiedOrOptimized = true;
			return new True();
		}

		if (f instanceof Null) {
			simplifiedOrOptimized = true;
			return f;
		}

		if (f instanceof Not) {
			simplifiedOrOptimized = true;
			Not not = (Not) f;
			return not.formula.simplify();
		}

		if (f instanceof And) {
			simplifiedOrOptimized = true;
			And and = (And) f;
			Formula left = and.leftHandSide;
			Formula right = and.rightHandSide;
			return new Or(new Not(left), new Not(right)).simplify();
		}

		if (f instanceof Or) {
			simplifiedOrOptimized = true;
			Or or = (Or) f;
			Formula left = or.leftHandSide;
			Formula right = or.rightHandSide;
			return new And(new Not(left), new Not(right)).simplify();
		}

		return new Not(f);
	}

	@Override
	public double getSelectivity() {
		double selectivity = 1 - formula.getSelectivity();
		logger.finer("selectivity[" + this + "] = " + selectivity);
		return selectivity;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Not) {
			Not not = (Not) o;
			return formula.equals(not.formula);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hc = 23;
		int multiplier = 37;
		return hc * multiplier + formula.hashCode();
	}
}
