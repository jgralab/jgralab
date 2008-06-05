/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;

/**
 * Merges all constraint {@link Expression}s that are connected to a
 * {@link Declaration} with {@link IsConstraintOf} edges by creating a
 * conjunction of all of them.
 * 
 * @author Tassilo Horn (horn), 2008
 * 
 */
public class MergeConstraintsOptimizer extends OptimizerBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz.jgralab.greql2.optimizer.Optimizer)
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
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator,
	 *      de.uni_koblenz.jgralab.greql2.schema.Greql2)
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
			IsConstraintOf constraint = decl.getFirstIsConstraintOf();
			while (constraint != null) {
				constraintEdges.add(constraint);
				constraint = constraint.getNextIsConstraintOf();
			}
			if (constraintEdges.size() > 1) {
				constraintsGotMerged = true;
				Expression singleConstraint = OptimizerUtility
						.createConjunction(constraintEdges, syntaxgraph);
				for (IsConstraintOf e : constraintEdges) {
					e.delete();
				}
				syntaxgraph.createIsConstraintOf(singleConstraint, decl);
			}
		}
		return constraintsGotMerged;
	}

}
