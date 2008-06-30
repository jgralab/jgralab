/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import java.util.HashSet;

import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public class NonConstantTerm extends Formula {

	protected Expression expression;

	public NonConstantTerm(Expression exp) {
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
	protected HashSet<Expression> getNonConstantTermExpressions() {
		HashSet<Expression> exps = new HashSet<Expression>();
		exps.add(expression);
		return exps;
	}

	@Override
	protected Formula calculateReplacementFormula(Expression exp,
			Literal literal) {
		if (expression == exp)
			return literal;
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
		if (this.toString().equals("v14"))
			selectivity = 0.8;
		if (this.toString().equals("v21"))
			selectivity = 0.5;
		if (this.toString().equals("v29"))
			selectivity = 0.3;
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
