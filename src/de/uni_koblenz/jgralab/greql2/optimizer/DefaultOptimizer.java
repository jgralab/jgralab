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

import java.util.Set;

import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public class DefaultOptimizer extends OptimizerBase {

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
	public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
			throws OptimizerException {
		// printGraphAsDot(syntaxgraph, "before-optimization");

		// optimizers
		Optimizer cso = new CommonSubgraphOptimizer();
		Optimizer eso = new EarySelectionOptimizer();
		Optimizer peo = new PathExistenceOptimizer();
		Optimizer vdoo = new VariableDeclarationOrderOptimizer();
		Optimizer ceo = new ConditionalExpressionOptimizer();
		Optimizer txfao = new TransformXorFunctionApplicationOptimizer();

		// do the optimization
		boolean aTransformationWasDone = cso.optimize(eval, syntaxgraph)
				| txfao.optimize(eval, syntaxgraph) |

				cso.optimize(eval, syntaxgraph)
				| eso.optimize(eval, syntaxgraph) |

				cso.optimize(eval, syntaxgraph)
				| vdoo.optimize(eval, syntaxgraph) |

				cso.optimize(eval, syntaxgraph)
				| peo.optimize(eval, syntaxgraph) |

				cso.optimize(eval, syntaxgraph)
				| ceo.optimize(eval, syntaxgraph) |

				cso.optimize(eval, syntaxgraph);

		// printGraphAsDot(syntaxgraph, "after-optimization");
		// printCosts(eval, syntaxgraph);

		return aTransformationWasDone;
	}

	private void printCosts(GreqlEvaluator eval, Greql2 syntaxgraph) {
		System.out.println("Optimizer: Optimizing " + syntaxgraph.getId()
				+ ".\n" + "This syntaxgraph has " + syntaxgraph.getECount()
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
		System.out
				.println("=========================================================");
		while (vertex != null) {
			System.out.println("Current Node: " + vertex);
			veval = marker.getMark(vertex);
			if (veval != null) {
				long costs = veval.getInitialSubtreeEvaluationCosts(graphSize);
				long card = veval.getEstimatedCardinality(graphSize);
				Set<Variable> neededVars = veval.getNeededVariables();
				Set<Variable> definedVars = veval.getDefinedVariables();
				long varCombs = veval.getVariableCombinations(graphSize);
				double sel = veval.getEstimatedSelectivity(graphSize);
				System.out.println("Costs for subtree evaluation: " + costs
						+ "\n" + "Estimated cardinality: " + card + "\n"
						+ "Estimated selectivity: " + sel + "\n"
						+ "Needed Vars: " + neededVars + "\n"
						+ "Defined Vars: " + definedVars + "\n"
						+ "Variable Combinations: " + varCombs);
			}
			System.out
					.println("=========================================================");
			vertex = vertex.getNextGreql2Vertex();
		}
		VertexEvaluator greql2ExpEval = marker.getMark(syntaxgraph
				.getFirstGreql2Expression());
		greql2ExpEval.resetSubtreeToInitialState();
		long estimatedInterpretationSteps = greql2ExpEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		System.out.println("Costs for the whole query: "
				+ estimatedInterpretationSteps);
	}
}
