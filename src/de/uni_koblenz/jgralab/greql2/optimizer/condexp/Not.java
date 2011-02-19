/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Not extends Formula {

	protected Formula formula;

	public Not(GreqlEvaluator eval, Formula formula) {
		super(eval);
		this.formula = formula;
	}

	@Override
	public String toString() {
		return "~" + formula;
	}

	@Override
	public Expression toExpression() {
		Greql2 syntaxgraph = greqlEvaluator.getSyntaxGraph();
		FunctionApplication funApp = syntaxgraph.createFunctionApplication();
		FunctionId funId = OptimizerUtility.findOrCreateFunctionId("not",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(funId, funApp);
		syntaxgraph.createIsArgumentOf(formula.toExpression(), funApp);
		return funApp;
	}

	@Override
	protected ArrayList<Expression> getNonConstantTermExpressions() {
		return formula.getNonConstantTermExpressions();
	}

	@Override
	protected Formula calculateReplacementFormula(Expression exp,
			Literal literal) {
		return new Not(greqlEvaluator, formula.calculateReplacementFormula(exp,
				literal));
	}

	@Override
	public Formula simplify() {
		Formula f = formula.simplify();

		if (f instanceof True) {
			return new False(greqlEvaluator);
		}

		if (f instanceof False) {
			return new True(greqlEvaluator);
		}

		if (f instanceof Not) {
			Not not = (Not) f;
			return not.formula.simplify();
		}

		if (f instanceof And) {
			And and = (And) f;
			Formula left = and.leftHandSide;
			Formula right = and.rightHandSide;
			return new Or(greqlEvaluator, new Not(greqlEvaluator, left),
					new Not(greqlEvaluator, right)).simplify();
		}

		if (f instanceof Or) {
			Or or = (Or) f;
			Formula left = or.leftHandSide;
			Formula right = or.rightHandSide;
			return new And(greqlEvaluator, new Not(greqlEvaluator, left),
					new Not(greqlEvaluator, right)).simplify();
		}

		return new Not(greqlEvaluator, f);
	}

	@Override
	public double getSelectivity() {
		double selectivity = 1 - formula.getSelectivity();
		logger.finer("selectivity[" + this + "] = " + selectivity);
		return selectivity;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Not) {
			Not not = (Not) o;
			return formula.equals(not.formula);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hc = 23;
		int multiplier = 37;
		return hc * multiplier + formula.hashCode();
	}
}
