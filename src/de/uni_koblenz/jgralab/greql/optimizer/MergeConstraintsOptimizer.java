/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
/**
 *
 */
package de.uni_koblenz.jgralab.greql.optimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql.schema.Declaration;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql.schema.FunctionId;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.IsConstraintOf;

/**
 * Merges all constraint {@link Expression}s that are connected to a
 * {@link Declaration} with {@link IsConstraintOf} edges by creating a
 * conjunction of all of them.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class MergeConstraintsOptimizer extends OptimizerBase {

	private static Logger logger = JGraLab
			.getLogger(MergeConstraintsOptimizer.class);

	MergeConstraintsOptimizer(OptimizerInfo optimizerInfo) {
		super(optimizerInfo);
	}

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
	 * de.uni_koblenz.jgralab.greql2.schema.Greql)
	 */
	@Override
	public boolean optimize(GreqlQuery query) throws OptimizerException {
		GreqlGraph syntaxgraph = query.getQueryGraph();

		ArrayList<Declaration> declarations = new ArrayList<>();
		for (Declaration decl : syntaxgraph.getDeclarationVertices()) {
			declarations.add(decl);
		}

		boolean constraintsGotMerged = false;
		for (Declaration decl : declarations) {
			ArrayList<IsConstraintOf> constraintEdges = new ArrayList<>();
			for (IsConstraintOf constraint : decl
					.getIsConstraintOfIncidences(EdgeDirection.IN)) {
				constraintEdges.add(constraint);
				constraint = constraint.getNextIsConstraintOfIncidence();
			}
			if (constraintEdges.size() > 1) {
				constraintsGotMerged = true;
				Expression singleConstraint = createConjunction(
						constraintEdges, syntaxgraph);

				logger.finer(optimizerHeaderString()
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
			GreqlGraph syntaxgraph) {
		if (constraintEdges.size() == 1) {
			return constraintEdges.get(0).getAlpha();
		}
		FunctionApplication funApp = syntaxgraph.createFunctionApplication();
		FunctionId funId = OptimizerUtility.findOrCreateFunctionId("and",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(funId, funApp);
		syntaxgraph.createIsArgumentOf(constraintEdges.get(0).getAlpha(),
				funApp);
		syntaxgraph.createIsArgumentOf(
				createConjunction(
						constraintEdges.subList(1, constraintEdges.size()),
						syntaxgraph), funApp);
		return funApp;
	}
}
