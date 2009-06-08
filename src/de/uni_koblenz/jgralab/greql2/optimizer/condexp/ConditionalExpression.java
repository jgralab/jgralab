/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 *
 * @author ist@uni-koblenz.de
 *
 */
public class ConditionalExpression extends Formula {
	protected Expression condition;
	protected Formula trueFormula, nullFormula, falseFormula;

	public ConditionalExpression(Expression condition, Formula trueExp,
			Formula falseExp, Formula nullExp) {
		this.condition = condition;
		trueFormula = trueExp;
		falseFormula = falseExp;
		nullFormula = nullExp;
	}

	@Override
	public String toString() {
		return "(v" + condition.getId() + ") ? " + trueFormula + " : "
				+ falseFormula + " : " + nullFormula + ";";
	}

	@Override
	public Expression toExpression() {
		de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression cond = syntaxgraph
				.createConditionalExpression();
		syntaxgraph.createIsConditionOf(condition, cond);
		syntaxgraph.createIsTrueExprOf(trueFormula.toExpression(), cond);
		syntaxgraph.createIsFalseExprOf(falseFormula.toExpression(), cond);
		syntaxgraph.createIsNullExprOf(nullFormula.toExpression(), cond);
		return cond;
	}

	@Override
	protected ArrayList<Expression> getNonConstantTermExpressions() {
		throw new UnsupportedOperationException(
				"Intentionally not implemented.");
	}

	@Override
	protected Formula calculateReplacementFormula(Expression exp,
			Literal literal) {
		throw new UnsupportedOperationException(
				"Intentionally not implemented.");
	}

	@Override
	public Formula simplify() {
		throw new UnsupportedOperationException(
				"Intentionally not implemented.");
	}

	@Override
	public double getSelectivity() {
		throw new UnsupportedOperationException(
				"Intentionally not implemented.");
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException(
				"Intentionally not implemented.");
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException(
				"Intentionally not implemented.");
	}
}
