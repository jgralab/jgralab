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
	private Formula trueFormula, falseFormula, nullFormula;
	private double influenceCostRatio = -1;

	public ConditionalExpressionUnit(Expression exp, Formula origFormula) {
		greqlEvaluator = origFormula.greqlEvaluator;
		condition = exp;
		trueFormula = origFormula.calculateReplacementFormula(condition,
				new True(greqlEvaluator)).simplify();
		falseFormula = origFormula.calculateReplacementFormula(condition,
				new False(greqlEvaluator)).simplify();
		nullFormula = origFormula.calculateReplacementFormula(condition,
				new Null(greqlEvaluator)).simplify();
	}

	private double calculateInfluenceCostRatio() {
		Formula boolDiff = new Not(greqlEvaluator, new Equiv(greqlEvaluator,
				trueFormula, new Not(greqlEvaluator, new Equiv(greqlEvaluator,
						falseFormula, nullFormula))));
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
				.optimize(), falseFormula.optimize(), nullFormula.optimize());
	}

	public double getInfluenceCostRatio() {
		if (influenceCostRatio == -1) {
			influenceCostRatio = calculateInfluenceCostRatio();
		}
		return influenceCostRatio;
	}

}
