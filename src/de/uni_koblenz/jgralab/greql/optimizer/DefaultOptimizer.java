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

package de.uni_koblenz.jgralab.greql.optimizer;

import java.util.Set;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.schema.Variable;

/**
 * @author ist@uni-koblenz.de
 * 
 */
public class DefaultOptimizer extends OptimizerBase {

	private static Logger logger = JGraLab.getLogger(DefaultOptimizer.class);

	public DefaultOptimizer(OptimizerInfo optimizerInfo) {
		super(optimizerInfo);
	}

	@Override
	protected String optimizerHeaderString() {
		return "### " + this.getClass().getSimpleName() + ": ";
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
		if (optimizer instanceof DefaultOptimizer) {
			return true;
		} else {
			return false;
		}
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

		if (query.getQueryGraph().getVCount() <= 1) {
			return false;
		}

		logger.fine(optimizerHeaderString()
				+ "Starting optimization.  Fasten your seatbelts!");

		// Tg2Dot.printGraphAsDot(syntaxgraph, true,
		// "/home/horn/before-optimization.tg");

		// optimizers
		Optimizer cso = new CommonSubgraphOptimizer(optimizerInfo);
		Optimizer pe2dpeo = new PathExistenceToDirectedPathExpressionOptimizer(
				optimizerInfo);
		Optimizer eso = new EarlySelectionOptimizer(optimizerInfo);
		Optimizer peo = new PathExistenceOptimizer(optimizerInfo);
		Optimizer vdoo = new VariableDeclarationOrderOptimizer(optimizerInfo);
		Optimizer ceo = new ConditionalExpressionOptimizer(optimizerInfo);
		Optimizer txfao = new TransformXorFunctionApplicationOptimizer(
				optimizerInfo);
		Optimizer mco = new MergeConstraintsOptimizer(optimizerInfo);
		Optimizer msdo = new MergeSimpleDeclarationsOptimizer(optimizerInfo);

		int noOfRuns = 0;

		// try to get more precise estimations when optimizer info is schema
		// specific.
		if (optimizerInfo.getSchema() != null) {
			TypeCollectionEvaluator tce = new TypeCollectionEvaluator(
					(GreqlQueryImpl) query);
			tce.execute();
		}

		// do the optimization
		boolean opt;
		do {
			// First merge common subgraphs
			opt = cso.optimize(query);

			// then transform all Xors to (x & ~y) | (~x & y).
			// if anything was changed, merge common subgraphs that may be the
			// result of the previous step.
			if (txfao.optimize(query)) {
				opt = true;
				cso.optimize(query);
			}

			// For each declaration merge its constraints into a single
			// conjunction.
			boolean runCso = false;
			if (mco.optimize(query)) {
				runCso = true;
				opt = true;
			}

			if (mco.optimize(query)) {
				runCso = true;
				opt = true;
			}

			// Then try to pull up path existences as forward/backward
			// vertex sets into the type expressions of the start or target
			// expression variable.
			if (pe2dpeo.optimize(query)) {
				runCso = true;
				opt = true;
			}

			// Now move predicates that are part of a conjunction and thus
			// movable into the type expression of the simple declaration
			// that declares all needed local variables of it.
			if (eso.optimize(query)) {
				runCso = true;
				opt = true;
			}

			if (runCso) {
				// Merge common subgraphs again.
				cso.optimize(query);
			}

			// Reorder the variable declarations in all declaration vertices
			// so that these assertions hold: 1. Variables which cause high
			// recalculation costs on value changes are declared first. 2.
			// If two variables cause the same recalculation costs the
			// variable with lower cardinality is declared before the other
			// one.
			if (vdoo.optimize(query)) {
				opt = true;
				// Now merge the common subgraphs again.
				cso.optimize(query);
			}

			runCso = false;
			// Transform path existence predicates to function applications
			// of the "contains" function.
			if (peo.optimize(query)) {
				runCso = true;
				opt = true;
			}

			// Transform complex constraint expressions to conditional
			// expressions to simulate short circuit evaluation.
			if (ceo.optimize(query)) {
				runCso = true;
				opt = true;
			}

			if (runCso) {
				// At last, merge common subgraphs and
				cso.optimize(query);
			}

			// merge simple declarations which have the same type
			// expression.
			opt |= msdo.optimize(query);

			if (opt) {
				noOfRuns++;
			}
			logger.fine(optimizerHeaderString() + "starts a new iteration ("
					+ noOfRuns + ")...");
		} while (opt && noOfRuns < 10);
		if (noOfRuns >= 10) {
			logger.warning("Optimizer didn't finish after 10 runs. Stopping here.");
		} else {
			logger.fine(optimizerHeaderString() + " finished after " + noOfRuns
					+ " iterations.");
		}

		// Tg2Dot.printGraphAsDot(syntaxgraph, true,
		// "/home/horn/after-optimization.tg");

		// System.out.println("DefaultOptimizer: "
		// + ((SerializableGreql) syntaxgraph).serialize());

		return noOfRuns > 0;
	}

	protected void printCosts(GreqlQuery query) {
		GreqlGraph syntaxgraph = query.getQueryGraph();

		logger.fine("Optimizer: Optimizing " + syntaxgraph.getId() + ".\n"
				+ "This syntaxgraph has " + syntaxgraph.getECount()
				+ " edges and " + syntaxgraph.getVCount() + " vertexes.");
		VertexEvaluator<? extends GreqlVertex> veval;

		// Calculate the cost of the root vertex so that all initial costs of
		// the vertices below are properly initialized.
		GreqlExpression rootVertex = syntaxgraph.getFirstGreqlExpression();
		VertexEvaluator<GreqlExpression> rootEval = ((GreqlQueryImpl) query)
				.getVertexEvaluator(rootVertex);
		rootEval.getInitialSubtreeEvaluationCosts();
		rootEval.getEstimatedCardinality();
		rootEval.calculateEstimatedSelectivity();

		GreqlVertex vertex = syntaxgraph.getFirstGreqlVertex();
		logger.fine("=========================================================");
		while (vertex != null) {
			logger.fine("Current Node: " + vertex);
			veval = ((GreqlQueryImpl) query).getVertexEvaluator(vertex);
			if (veval != null) {
				long costs = veval.getInitialSubtreeEvaluationCosts();
				long card = veval.getEstimatedCardinality();
				Set<Variable> neededVars = veval.getNeededVariables();
				Set<Variable> definedVars = veval.getDefinedVariables();
				long varCombs = veval.getVariableCombinations();
				double sel = veval.getEstimatedSelectivity();
				logger.fine("Costs for subtree evaluation: " + costs + "\n"
						+ "Estimated cardinality: " + card + "\n"
						+ "Estimated selectivity: " + sel + "\n"
						+ "Needed Vars: " + neededVars + "\n"
						+ "Defined Vars: " + definedVars + "\n"
						+ "Variable Combinations: " + varCombs);
			}
			logger.fine("=========================================================");
			vertex = vertex.getNextGreqlVertex();
		}
		VertexEvaluator<GreqlExpression> greql2ExpEval = ((GreqlQueryImpl) query)
				.getVertexEvaluator(syntaxgraph.getFirstGreqlExpression());
		greql2ExpEval.resetSubtreeToInitialState(null);
		long estimatedInterpretationSteps = greql2ExpEval
				.getCurrentSubtreeEvaluationCosts();
		logger.fine("Costs for the whole query: "
				+ estimatedInterpretationSteps);
	}
}
