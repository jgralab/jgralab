/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Equiv extends BinaryOperator {

	public Equiv(GreqlEvaluator eval, Formula lhs, Formula rhs) {
		super(eval, lhs, rhs);
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
		return new Equiv(greqlEvaluator, lhs, rhs);
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
