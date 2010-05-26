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
