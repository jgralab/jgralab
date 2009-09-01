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
public class Null extends Literal {

	public Null(GreqlEvaluator eval) {
		super(eval);
	}

	@Override
	public String toString() {
		return "null";
	}

	@Override
	public Expression toExpression() {
		BoolLiteral bool = greqlEvaluator.getSyntaxGraph().createBoolLiteral();
		bool.setBoolValue(TrivalentBoolean.NULL);
		return bool;
	}

	@Override
	public double getSelectivity() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Null;
	}

	@Override
	public int hashCode() {
		return 0;
	}

}
