/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

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
public class ConditionalExpressionUnit {

	private GreqlEvaluator greqlEvaluator;

	private Expression condition;
	private Formula trueFormula, falseFormula;
	private double influenceCostRatio = -1;

	public ConditionalExpressionUnit(Expression exp, Formula origFormula) {
		greqlEvaluator = origFormula.greqlEvaluator;
		condition = exp;
		trueFormula = origFormula.calculateReplacementFormula(condition,
				new True(greqlEvaluator)).simplify();
		falseFormula = origFormula.calculateReplacementFormula(condition,
				new False(greqlEvaluator)).simplify();
	}

	private double calculateInfluenceCostRatio() {
		Formula boolDiff = new Not(greqlEvaluator, new Equiv(greqlEvaluator,
				trueFormula, new Not(greqlEvaluator, 
						falseFormula)));
		boolDiff = boolDiff.simplify();

		// selectivity of the boolean difference
		double selectivity = boolDiff.getSelectivity();

		// costs of the condition expression
		VertexEvaluator veval = greqlEvaluator.getVertexEvaluatorGraphMarker()
				.getMark(condition);
		GraphSize graphSize = null;
		if (greqlEvaluator.getDatagraph() != null) {
			graphSize = new GraphSize(greqlEvaluator.getDatagraph());
		} else {
			graphSize = OptimizerUtility.getDefaultGraphSize();
		}
		long costs = veval.getInitialSubtreeEvaluationCosts(graphSize);
		return selectivity / costs;
	}

	ConditionalExpression toConditionalExpression() {
		return new ConditionalExpression(greqlEvaluator, condition, trueFormula
				.optimize(), falseFormula.optimize());
	}

	public double getInfluenceCostRatio() {
		if (influenceCostRatio == -1) {
			influenceCostRatio = calculateInfluenceCostRatio();
		}
		return influenceCostRatio;
	}

}
