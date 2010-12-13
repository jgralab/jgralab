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
import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class BinaryOperator extends Formula {
	protected Formula leftHandSide, rightHandSide;

	public BinaryOperator(GreqlEvaluator eval, Formula lhs, Formula rhs) {
		super(eval);
		leftHandSide = lhs;
		rightHandSide = rhs;
	}

	@Override
	protected ArrayList<Expression> getNonConstantTermExpressions() {
		ArrayList<Expression> exps = leftHandSide
				.getNonConstantTermExpressions();
		exps.addAll(rightHandSide.getNonConstantTermExpressions());
		return exps;
	}

	protected int hashCode(int startVal) {
		int multiplier = 59;
		startVal = startVal * multiplier + leftHandSide.hashCode();
		startVal = startVal * multiplier + rightHandSide.hashCode();
		return startVal;
	}

}
