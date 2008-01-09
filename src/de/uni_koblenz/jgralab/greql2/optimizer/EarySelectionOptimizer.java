/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.Literal;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * This {@link Optimizer} implements the transformation "Selection as early as
 * possible".
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public class EarySelectionOptimizer extends OptimizerBase {

	private GreqlEvaluator eval;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz.jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof EarySelectionOptimizer) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator,
	 *      de.uni_koblenz.jgralab.greql2.schema.Greql2)
	 */
	@Override
	public void optimize(GreqlEvaluator eval, Greql2 syntaxgraph) {
		this.eval = eval;

		for (Declaration decl : syntaxgraph.getDeclarationVertices()) {
			IsConstraintOf isConst = decl
					.getFirstIsConstraintOf(EdgeDirection.IN);
			while (isConst != null) {
				Expression exp = (Expression) isConst.getAlpha();
				findMovableExpression(exp);

				isConst = isConst.getNextIsConstraintOf(EdgeDirection.IN);
			}
		}

		for (Expression exp : movableExpressions) {
			System.out.println(exp + " is Movable!");
		}
	}

	private ArrayList<Expression> movableExpressions = new ArrayList<Expression>();

	private void findMovableExpression(Expression exp) {
		if (exp instanceof Literal) {
			return;
		}
		if (exp instanceof FunctionApplication) {
			FunctionApplication funApp = (FunctionApplication) exp;
			if (isAnd(funApp)) {
				// For AND expressions we dive deeper into the arguments.
				IsArgumentOf isArg = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				while (isArg != null) {
					findMovableExpression((Expression) isArg.getAlpha());
					isArg = isArg.getNextIsArgumentOf(EdgeDirection.IN);

				}
			} else if (isMovable(funApp)) {
				movableExpressions.add(funApp);
				return;
			}
		}
		System.err
				.println("No Literal and no FunctionApplication in EarlySelectionOptimizer.findMovableExpression(): "
						+ exp);
	}

	private boolean isMovable(Expression exp) {
		GraphMarker<VertexEvaluator> marker = eval
				.getVertexEvaluatorGraphMarker();
		VertexEvaluator veval = marker.getMark(exp);
		Set<Variable> neededVars = veval.getNeededVariables();
		if (neededVars.size() == 1) {
			return true;
		} else if (neededVars.isEmpty()) {
			// This shouldn't happen
			System.err
					.println("neededVars is empty in EarlySelectionOptimizer.isMovable("
							+ exp + ").");
		} else {
			SimpleDeclaration sd, oldSd = null;
			for (Variable var : neededVars) {
				sd = (SimpleDeclaration) var.getFirstIsDeclaredVarOf()
						.getOmega();
				if (oldSd != null && sd != oldSd) {
					// the last variable was declared in another
					// SimpleDeclaration
					return false;
				}
				oldSd = sd;
			}
		}
		return true;
	}

	private boolean isAnd(FunctionApplication funApp) {
		try {
			return funApp.getFirstIsFunctionIdOf().getAlpha().getAttribute(
					"name").equals("and");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		}
	}
}
