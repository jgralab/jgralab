/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.greql.optimizer.condexp;

import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql.schema.FunctionId;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class And extends BinaryOperator {

	public And(GreqlQuery query, Formula lhs, Formula rhs) {
		super(query, lhs, rhs);
	}

	@Override
	public String toString() {
		return "(" + leftHandSide + " & " + rightHandSide + ")";
	}

	@Override
	public Expression toExpression() {
		GreqlGraph syntaxgraph = query.getQueryGraph();
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
		return new And(query, leftHandSide.calculateReplacementFormula(exp,
				literal), rightHandSide.calculateReplacementFormula(exp,
				literal));
	}

	@Override
	public Formula simplify() {
		Formula lhs = leftHandSide.simplify();
		Formula rhs = rightHandSide.simplify();

		// BEWARE: (x & ~x) is NOT always false in GReQL, cause it's null, if x
		// evaluates to null...

		if (lhs.equals(new Not(query, rhs)) || new Not(query, lhs).equals(rhs)) {
			return new False(query);
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

		if (lhs.equals(rhs)) {
			return lhs;
		}

		return new And(query, lhs, rhs);
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
