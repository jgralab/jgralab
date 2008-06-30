/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public class Equiv extends BinaryOperator {

	public Equiv(Formula lhs, Formula rhs) {
		super(lhs, rhs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.optimizer.condexp.Formula#
	 * calculateReplacementFormula
	 * (de.uni_koblenz.jgralab.greql2.schema.Expression,
	 * de.uni_koblenz.jgralab.greql2.optimizer.condexp.Literal)
	 */
	@Override
	protected Formula calculateReplacementFormula(Expression exp,
			Literal literal) {
		throw new UnsupportedOperationException(
				"Intentionally not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.condexp.Formula#simplify()
	 */
	@Override
	public Formula simplify() {
		Formula lhs = leftHandSide.simplify();
		Formula rhs = rightHandSide.simplify();
		return new Equiv(lhs, rhs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.condexp.Formula#toExpression()
	 */
	@Override
	public Expression toExpression() {
		throw new UnsupportedOperationException(
				"Intentionally not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.condexp.Formula#toString()
	 */
	@Override
	public String toString() {
		return "(" + leftHandSide + " <=> " + rightHandSide + ")";
	}

	@Override
	public double getSelectivity() {
		double leftSel = leftHandSide.getSelectivity();
		double rightSel = rightHandSide.getSelectivity();
		double selectivity = 1 - (1 - leftSel * rightSel)
				* (1 - (1 - leftSel) * (1 - rightSel));
		logger.finer("selectivity[" + this + "] = " + selectivity);
		return selectivity;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Equiv) {
			Equiv equiv = (Equiv) o;
			return leftHandSide.equals(equiv.leftHandSide)
					&& rightHandSide.equals(equiv.rightHandSide);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode(21);
	}
}
