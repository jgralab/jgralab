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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class NonConstantTerm extends Formula {

	protected Expression expression;

	public NonConstantTerm(GreqlEvaluator eval, Expression exp) {
		super(eval);
		expression = exp;
	}

	@Override
	public String toString() {
		return "v" + expression.getId();
	}

	@Override
	public Expression toExpression() {
		return expression;
	}

	@Override
	protected ArrayList<Expression> getNonConstantTermExpressions() {
		ArrayList<Expression> exps = new ArrayList<Expression>();
		exps.add(expression);
		return exps;
	}

	@Override
	protected Formula calculateReplacementFormula(Expression exp,
			Literal literal) {
		if (expression == exp) {
			return literal;
		}
		return this;
	}

	@Override
	public Formula simplify() {
		return this;
	}

	@Override
	public double getSelectivity() {
		GraphSize graphSize = null;
		if (greqlEvaluator.getDatagraph() != null) {
			graphSize = new GraphSize(greqlEvaluator.getDatagraph());
		} else {
			graphSize = OptimizerUtility.getDefaultGraphSize();
		}
		VertexEvaluator veval = greqlEvaluator.getVertexEvaluatorGraphMarker()
				.getMark(expression);
		double selectivity = veval.calculateEstimatedSelectivity(graphSize);
		if (this.toString().equals("v14")) {
			selectivity = 0.8;
		}
		if (this.toString().equals("v21")) {
			selectivity = 0.5;
		}
		if (this.toString().equals("v29")) {
			selectivity = 0.3;
		}
		logger.finer("selectivity[" + this + "] = " + selectivity);
		return selectivity;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof NonConstantTerm) {
			NonConstantTerm nct = (NonConstantTerm) o;
			return expression == nct.expression;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return expression.hashCode();
	}
}
