package de.uni_koblenz.jgralab.greql2.optimizer.dissolution;

import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;

public class Not extends Leaf {

	protected SemanticGraph formula;

	public Not(SemanticGraph f) {
		formula = f;
	}

	@Override
	public String toString() {
		return "~" + formula.toString();
	}

	@Override
	public Expression toExpression(Greql2 syntaxgraph) {
		FunctionApplication not = syntaxgraph.createFunctionApplication();
		FunctionId notId = OptimizerUtility.findOrCreateFunctionId("not",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(notId, not);
		syntaxgraph.createIsArgumentOf(formula.toExpression(syntaxgraph), not);
		return not;
	}

	@Override
	public SemanticGraph toNegationNormalForm() {
		if (formula instanceof Not) {
			// we have not(not(someFormula), so get rid of it
			return ((Not) formula).formula.toNegationNormalForm();
		}
		if (formula instanceof BinaryOperator) {
			BinaryOperator subFormula = (BinaryOperator) formula;
			SemanticGraph lhs = subFormula.leftHandSide;
			SemanticGraph rhs = subFormula.rightHandSide;
			if (subFormula instanceof And) {
				// apply DeMorgan's law
				return new Or(new Not(lhs).toNegationNormalForm(), new Not(rhs)
						.toNegationNormalForm());
			} else if (subFormula instanceof Or) {
				// apply DeMorgan's law
				return new And(new Not(lhs).toNegationNormalForm(),
						new Not(rhs).toNegationNormalForm());
			} else {
				logger
						.severe("The NNF is only implemented via DeMorgan for And, Or and Xor!");
				return this;
			}
		}
		// The only remaining possibility is that formula is an Atom. If that's
		// the case it's already in NNF, so simply return it.
		return this;
	}

	@Override
	public boolean isEqualTo(SemanticGraph sg) {
		return this == sg;
	}

	@Override
	public long getCosts() {
		return formula.getCosts() + 2;
	}

	@Override
	public SemanticGraph deepCopy() {
		return new Not(formula.deepCopy());
	}
}
