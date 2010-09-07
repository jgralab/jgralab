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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ConditionalExpression extends Formula {
	protected Expression condition;
	protected Formula trueFormula, falseFormula;

	public ConditionalExpression(GreqlEvaluator eval, Expression condition,
			Formula trueExp, Formula falseExp) {
		super(eval);
		this.condition = condition;
		trueFormula = trueExp;
		falseFormula = falseExp;
	}

	@Override
	public String toString() {
		return "(v" + condition.getId() + ") ? " + trueFormula + " : "
				+ falseFormula + ";";
	}

	@Override
	public Expression toExpression() {
		Greql2 syntaxgraph = greqlEvaluator.getSyntaxGraph();
		de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression cond = syntaxgraph
				.createConditionalExpression();
		syntaxgraph.createIsConditionOf(condition, cond);
		syntaxgraph.createIsTrueExprOf(trueFormula.toExpression(), cond);
		syntaxgraph.createIsFalseExprOf(falseFormula.toExpression(), cond);
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
