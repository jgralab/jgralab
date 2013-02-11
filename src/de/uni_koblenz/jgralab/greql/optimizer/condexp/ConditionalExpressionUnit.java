/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ConditionalExpressionUnit {

	private final GreqlQuery query;

	private final Expression condition;
	private final Formula trueFormula;
	private final Formula falseFormula;
	private double influenceCostRatio = -1;

	public ConditionalExpressionUnit(Expression exp, Formula origFormula) {
		query = origFormula.query;
		condition = exp;
		trueFormula = origFormula.calculateReplacementFormula(condition,
				new True(query)).simplify();
		falseFormula = origFormula.calculateReplacementFormula(condition,
				new False(query)).simplify();
	}

	private double calculateInfluenceCostRatio() {
		Formula boolDiff = new Not(query, new Equiv(query, trueFormula,
				new Not(query, falseFormula)));
		boolDiff = boolDiff.simplify();

		// selectivity of the boolean difference
		double selectivity = boolDiff.getSelectivity();

		// costs of the condition expression
		VertexEvaluator<? extends Expression> veval = ((GreqlQueryImpl) query)
				.getVertexEvaluator(condition);
		long costs = veval.getInitialSubtreeEvaluationCosts();
		return selectivity / costs;
	}

	ConditionalExpression toConditionalExpression() {
		return new ConditionalExpression(query, condition,
				trueFormula.optimize(), falseFormula.optimize());
	}

	public double getInfluenceCostRatio() {
		if (influenceCostRatio == -1) {
			influenceCostRatio = calculateInfluenceCostRatio();
		}
		return influenceCostRatio;
	}

}
