/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.greql2.optimizer;

import java.io.File;
import java.io.IOException;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.utilities.Utility;

/**
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public class DefaultOptimizer implements Optimizer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz.jgralab.greql2.optimizer.Optimizer)
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
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator,
	 *      de.uni_koblenz.jgralab.greql2.schema.Greql2)
	 */
	public void optimize(GreqlEvaluator eval, Greql2 syntaxgraph) {
		// logCosts(eval, syntaxgraph);
		Optimizer optimizer = new CommonSubgraphOptimizer();

		// output the unoptimized syntax graph
		try {
			Utility.convertGraphToDot(eval.getSyntaxGraph(), File
					.createTempFile("syntaxgraph-before", ".dot")
					.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// optimize
		optimizer.optimize(eval, syntaxgraph);
		optimizer = new MergeSimpleDeclarationsOptimizer();
		optimizer.optimize(eval, syntaxgraph);
		optimizer = new EarySelectionOptimizer();
		optimizer.optimize(eval, syntaxgraph);
		
		// output the optimized syntax graph
		try {
			Utility.convertGraphToDot(eval.getSyntaxGraph(), File
					.createTempFile("syntaxgraph-after", ".dot")
					.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// print the costs for each vertex
		//printCosts(eval, syntaxgraph);
	}

//	private void printCosts(GreqlEvaluator eval, Greql2 syntaxgraph) {
//		System.out.println("Optimizer: Optimizing " + syntaxgraph.getId()
//				+ ".\n" + "This syntaxgraph has " + syntaxgraph.getECount()
//				+ " edges and " + syntaxgraph.getVCount() + " vertexes.");
//		GraphMarker<VertexEvaluator> marker = eval
//				.getVertexEvaluatorGraphMarker();
//		VertexEvaluator veval;
//		GraphSize graphSize = new GraphSize(syntaxgraph);
//
//		// Calculate the cost of the root vertex so that all initial costs of
//		// the vertices below are properly initialized.
//		Greql2Expression rootVertex = syntaxgraph.getFirstGreql2Expression();
//		VertexEvaluator rootEval = marker.getMark(rootVertex);
//		rootEval.getInitialSubtreeEvaluationCosts(graphSize);
//		rootEval.getEstimatedCardinality(graphSize);
//		rootEval.calculateEstimatedSelectivity(graphSize);
//
//		Greql2Vertex vertex = syntaxgraph.getFirstGreql2Vertex();
//		System.out
//				.println("=========================================================");
//		while (vertex != null) {
//			System.out.println("Current Node: " + vertex);
//			veval = marker.getMark(vertex);
//			if (veval != null) {
//				int costs = veval.getInitialSubtreeEvaluationCosts(graphSize);
//				int card = veval.getEstimatedCardinality(graphSize);
//				double sel = veval.getEstimatedSelectivity(graphSize);
//				System.out.println("Costs for subtree evaluation: " + costs
//						+ "\n" + "Estimated cardinality: " + card + "\n"
//						+ "Estimated selectivity: " + sel);
//			}
//			System.out
//					.println("=========================================================");
//			vertex = vertex.getNextGreql2Vertex();
//		}
//		VertexEvaluator greql2ExpEval = marker.getMark(syntaxgraph
//				.getFirstGreql2Expression());
//		greql2ExpEval.resetSubtreeToInitialState();
//		int estimatedInterpretationSteps = greql2ExpEval
//				.getCurrentSubtreeEvaluationCosts(graphSize);
//		System.out.println("Costs for the whole query: "
//				+ estimatedInterpretationSteps);
//	}
}
