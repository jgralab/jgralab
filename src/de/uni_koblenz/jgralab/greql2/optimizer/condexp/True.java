/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.TrivalentBoolean;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class True extends Literal {

	public True(GreqlEvaluator eval) {
		super(eval);
	}

	@Override
	public String toString() {
		return "true";
	}

	@Override
	public Expression toExpression() {
		BoolLiteral bool = greqlEvaluator.getSyntaxGraph().createBoolLiteral();
		bool.setBoolValue(TrivalentBoolean.TRUE);
		return bool;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof True;
	}

	@Override
	public int hashCode() {
		return 1;
	}
}
