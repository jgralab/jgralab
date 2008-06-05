/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.optimizer.condexp.Formula;
import de.uni_koblenz.jgralab.greql2.optimizer.condexp.ConditionalExpressionUnit;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public class ConditionalExpressionOptimizer extends OptimizerBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz.jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof ConditionalExpressionOptimizer)
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator,
	 *      de.uni_koblenz.jgralab.greql2.schema.Greql2)
	 */
	@Override
	public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
			throws OptimizerException {
		// printGraphAsDot(syntaxgraph, "before-condexp");

		Formula.setSimplifiedOrOptimized(false);
		Formula.setGreqlEvaluator(eval);
		ConditionalExpressionUnit.setGreqlEvaluator(eval);

		ArrayList<Declaration> declarations = new ArrayList<Declaration>();
		for (Declaration decl : syntaxgraph.getDeclarationVertices()) {
			declarations.add(decl);
		}
		for (Declaration decl : declarations) {
			if (decl.getFirstIsConstraintOf() == null)
				continue;

			Expression topLevelExpression = (Expression) decl
					.getFirstIsConstraintOf().getAlpha();
			decl.getFirstIsConstraintOf().delete();
			Formula formula = Formula
					.createFormulaFromExpression(topLevelExpression);
			System.out.println(optimizerHeaderString()
					+ "Transformed constraint\n    " + formula);
			formula = formula.simplify().optimize();
			System.out.println("to\n    " + formula + ".");
			Expression newConstraint = formula.toExpression();
			syntaxgraph.createIsConstraintOf(newConstraint, decl);
		}

		try {
			eval.createVertexEvaluators();
		} catch (EvaluateException e) {
			e.printStackTrace();
			throw new OptimizerException(
					"Exception while re-creating VertexEvaluators.", e);
		}

		OptimizerUtility.createMissingSourcePositions(syntaxgraph);

		// printGraphAsDot(syntaxgraph, "after-condexp");

		return Formula.isSimplifiedOrOptimized();
	}
}
