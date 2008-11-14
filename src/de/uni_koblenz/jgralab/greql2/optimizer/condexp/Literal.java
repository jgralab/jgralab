/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import java.util.HashSet;

import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class Literal extends Formula {

	@Override
	protected HashSet<Expression> getNonConstantTermExpressions() {
		return new HashSet<Expression>();
	}

	@Override
	protected Formula calculateReplacementFormula(Expression exp,
			Literal literal) {
		return this;
	}

	@Override
	public Formula simplify() {
		return this;
	}
}
