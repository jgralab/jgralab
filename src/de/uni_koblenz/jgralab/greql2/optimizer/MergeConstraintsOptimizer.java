/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;

/**
 * Merges all constraint {@link Expression}s that are connected to a
 * {@link Declaration} with {@link IsConstraintOf} edges by creating a
 * conjunction of all of them.
 *
 * @author ist@uni-koblenz.de
 *
 */
public class MergeConstraintsOptimizer extends OptimizerBase {

	private static Logger logger = Logger
			.getLogger(MergeConstraintsOptimizer.class.getName());

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz
	 * .jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof MergeConstraintsOptimizer) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz
	 * .jgralab.greql2.evaluator.GreqlEvaluator,
	 * de.uni_koblenz.jgralab.greql2.schema.Greql2)
	 */
	@Override
	public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
			throws OptimizerException {
		ArrayList<Declaration> declarations = new ArrayList<Declaration>();
		for (Declaration decl : syntaxgraph.getDeclarationVertices()) {
			declarations.add(decl);
		}

		boolean constraintsGotMerged = false;
		for (Declaration decl : declarations) {
			ArrayList<IsConstraintOf> constraintEdges = new ArrayList<IsConstraintOf>();
			for (IsConstraintOf constraint : decl
					.getIsConstraintOfIncidences(EdgeDirection.IN)) {
				constraintEdges.add(constraint);
				constraint = constraint.getNextIsConstraintOf();
			}
			if (constraintEdges.size() > 1) {
				constraintsGotMerged = true;
				Expression singleConstraint = createConjunction(
						constraintEdges, syntaxgraph);

				logger.info(optimizerHeaderString()
						+ "Merging constraints on edges " + constraintEdges
						+ " into conjunction " + singleConstraint + ".");

				for (IsConstraintOf e : constraintEdges) {
					e.delete();
				}
				syntaxgraph.createIsConstraintOf(singleConstraint, decl);
			}
		}
		return constraintsGotMerged;
	}

	/**
	 * Given a list of {@link IsConstraintOf} edges of a {@link Declaration}
	 * create one {@link Expression} that can serve as single constraint. If the
	 * list contains exactly one {@link IsConstraintOf} edge, return its alpha
	 * expression. Else create AND {@link FunctionApplication}s combining all
	 * constraints.
	 *
	 * @param constraintEdges
	 * @param syntaxgraph
	 * @return a conjunction of all constraints
	 */
	public Expression createConjunction(List<IsConstraintOf> constraintEdges,
			Greql2 syntaxgraph) {
		if (constraintEdges.size() == 1) {
			return (Expression) constraintEdges.get(0).getAlpha();
		}
		FunctionApplication funApp = syntaxgraph.createFunctionApplication();
		FunctionId funId = OptimizerUtility.findOrCreateFunctionId("and",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(funId, funApp);
		syntaxgraph.createIsArgumentOf((Expression) constraintEdges.get(0)
				.getAlpha(), funApp);
		syntaxgraph.createIsArgumentOf(createConjunction(constraintEdges
				.subList(1, constraintEdges.size()), syntaxgraph), funApp);
		return funApp;
	}
}
