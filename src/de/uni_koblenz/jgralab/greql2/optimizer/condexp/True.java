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
public class True extends Literal {

	@Override
	public String toString() {
		return "true";
	}

	@Override
	public Expression toExpression() {
		BoolLiteral bool = syntaxgraph.createBoolLiteral();
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
