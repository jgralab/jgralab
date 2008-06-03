/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.condexp;

import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.TrivalentBoolean;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public class False extends Literal {

	@Override
	public String toString() {
		return "false";
	}

	@Override
	public Expression toExpression() {
		BoolLiteral bool = syntaxgraph.createBoolLiteral();
		bool.setBoolValue(TrivalentBoolean.FALSE);
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
