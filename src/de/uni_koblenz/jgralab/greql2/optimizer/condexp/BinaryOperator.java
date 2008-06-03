/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import java.util.HashSet;

import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public abstract class BinaryOperator extends Formula {
	protected Formula leftHandSide, rightHandSide;

	public BinaryOperator(Formula lhs, Formula rhs) {
		leftHandSide = lhs;
		rightHandSide = rhs;
	}

	@Override
	protected HashSet<Expression> getNonConstantTermExpressions() {
		HashSet<Expression> exps = leftHandSide.getNonConstantTermExpressions();
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
