/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.Set;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * @author ist@uni-koblenz.de
 * 
 */
public class DefaultOptimizer extends OptimizerBase {

	private static Logger logger = JGraLab.getLogger(DefaultOptimizer.class
			.getPackage().getName());

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
	 * de.uni_koblenz.jgralab.greql2.schema.Greql2)
	 */
	public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
			throws OptimizerException {
		logger.fine(optimizerHeaderString()
				+ "Starting optimization.  Fasten your seatbelts!");

		// Tg2Dot.printGraphAsDot(syntaxgraph, true,
		// "/home/horn/before-optimization.tg");

		// optimizers
		Optimizer cso = new CommonSubgraphOptimizer();
		Optimizer pe2dpeo = new PathExistenceToDirectedPathExpressionOptimizer();
		Optimizer eso = new EarlySelectionOptimizer();
		Optimizer peo = new PathExistenceOptimizer();
		Optimizer vdoo = new VariableDeclarationOrderOptimizer();
		Optimizer ceo = new ConditionalExpressionOptimizer();
		Optimizer txfao = new TransformXorFunctionApplicationOptimizer();
		Optimizer mco = new MergeConstraintsOptimizer();
		Optimizer msdo = new MergeSimpleDeclarationsOptimizer();

		boolean aTransformationWasDone = false;
		int noOfRuns = 1;

		// do the optimization
		while (
		// First merge common subgraphs
		cso.optimize(eval, syntaxgraph)
		// then transform all Xors to (x & ~y) | (~x & y).
				| txfao.optimize(eval, syntaxgraph)
				// Again, merge common subgraphs that may be the result of the
				// previous step.
				| cso.optimize(eval, syntaxgraph)
				// For each declaration merge its constraints into a single
				// conjunction.
				| mco.optimize(eval, syntaxgraph)
				// Then try to pull up path existences as forward/backward
				// vertex sets into the type expressions of the start or target
				// expression variabse.
				| pe2dpeo.optimize(eval, syntaxgraph)
				// Now move predicates that are part of a conjunction and thus
				// movable into the type expression of the simple declaration
				// that declares all needed local variables of it.
				| eso.optimize(eval, syntaxgraph)
				// Merge common subgraphs again.
				| cso.optimize(eval, syntaxgraph)
				// Reorder the variable declarations in all declaration vertices
				// so that these assertions hold: 1. Variables which cause high
				// recalculation costs on value changes are declared first. 2.
				// If two variables cause the same recalculation costs the
				// variable with lower cardinality is declared before the other
				// one.
				| vdoo.optimize(eval, syntaxgraph)
				// Now merge the common subgraphs again.
				| cso.optimize(eval, syntaxgraph)
				// Transform path existence predicates to function applications
				// of the "contains" function.
				| peo.optimize(eval, syntaxgraph)
				// Transform complex constraint expressions to conditional
				// expressions to simulate short circuit evaluation.
				| ceo.optimize(eval, syntaxgraph)
				// At last, merge common subgraphs and
				| cso.optimize(eval, syntaxgraph)
				// merge simple declarations which have the same type
				// expression.
				| msdo.optimize(eval, syntaxgraph)) {
			aTransformationWasDone = true;
			noOfRuns++;

			if (noOfRuns > 10) {
				logger
						.warning("Optimizer didn't finish after 10 runs. Stopping here.");
				break;
			}

			logger.fine(optimizerHeaderString() + "starts a new iteration ("
					+ noOfRuns + ")...");
		}
		;

		// Tg2Dot.printGraphAsDot(syntaxgraph, true,
		// "/home/horn/after-optimization.tg");

		// System.out.println("DefaultOptimizer: "
		// + ((SerializableGreql2) syntaxgraph).serialize());

		logger.fine(optimizerHeaderString() + " finished after " + noOfRuns
				+ " iterations.");
		return aTransformationWasDone;
	}

	@SuppressWarnings("unused")
	private void printCosts(GreqlEvaluator eval, Greql2 syntaxgraph) {
		logger.fine("Optimizer: Optimizing " + syntaxgraph.getId() + ".\n"
				+ "This syntaxgraph has " + syntaxgraph.getECount()
				+ " edges and " + syntaxgraph.getVCount() + " vertexes.");
		GraphMarker<VertexEvaluator> marker = eval
				.getVertexEvaluatorGraphMarker();
		VertexEvaluator veval;
		GraphSize graphSize = new GraphSize(syntaxgraph);

		// Calculate the cost of the root vertex so that all initial costs of
		// the vertices below are properly initialized.
		Greql2Expression rootVertex = syntaxgraph.getFirstGreql2Expression();
		VertexEvaluator rootEval = marker.getMark(rootVertex);
		rootEval.getInitialSubtreeEvaluationCosts(graphSize);
		rootEval.getEstimatedCardinality(graphSize);
		rootEval.calculateEstimatedSelectivity(graphSize);

		Greql2Vertex vertex = syntaxgraph.getFirstGreql2Vertex();
		logger
				.fine("=========================================================");
		while (vertex != null) {
			logger.fine("Current Node: " + vertex);
			veval = marker.getMark(vertex);
			if (veval != null) {
				long costs = veval.getInitialSubtreeEvaluationCosts(graphSize);
				long card = veval.getEstimatedCardinality(graphSize);
				Set<Variable> neededVars = veval.getNeededVariables();
				Set<Variable> definedVars = veval.getDefinedVariables();
				long varCombs = veval.getVariableCombinations(graphSize);
				double sel = veval.getEstimatedSelectivity(graphSize);
				logger.fine("Costs for subtree evaluation: " + costs + "\n"
						+ "Estimated cardinality: " + card + "\n"
						+ "Estimated selectivity: " + sel + "\n"
						+ "Needed Vars: " + neededVars + "\n"
						+ "Defined Vars: " + definedVars + "\n"
						+ "Variable Combinations: " + varCombs);
			}
			logger
					.fine("=========================================================");
			vertex = vertex.getNextGreql2Vertex();
		}
		VertexEvaluator greql2ExpEval = marker.getMark(syntaxgraph
				.getFirstGreql2Expression());
		greql2ExpEval.resetSubtreeToInitialState();
		long estimatedInterpretationSteps = greql2ExpEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		logger.fine("Costs for the whole query: "
				+ estimatedInterpretationSteps);
	}
}
