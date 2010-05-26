/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Expression;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class False extends Literal {

	public False(GreqlEvaluator eval) {
		super(eval);
	}

	@Override
	public String toString() {
		return "false";
	}

	@Override
	public Expression toExpression() {
		BoolLiteral bool = greqlEvaluator.getSyntaxGraph().createBoolLiteral();
		bool.set_boolValue(false);
		return bool;
	}

	@Override
	public double getSelectivity() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof False;
	}

	@Override
	public int hashCode() {
		return -1;
	}

}
