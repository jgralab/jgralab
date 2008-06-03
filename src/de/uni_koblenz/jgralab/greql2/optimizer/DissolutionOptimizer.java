/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.optimizer.dissolution.Atom;
import de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public class DissolutionOptimizer extends OptimizerBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz.jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof DissolutionOptimizer)
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
	public void optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
			throws OptimizerException {

		// Replace all Xor funapps with and/or/not...
		Optimizer xorTransform = new TransformXorFunctionApplicationOptimizer();
		xorTransform.optimize(eval, syntaxgraph);

		Atom.resetNumbering();

		GraphSize graphSize;
		if (eval.getDatagraph() != null) {
			graphSize = new GraphSize(eval.getDatagraph());
		} else {
			graphSize = OptimizerUtility.getDefaultGraphSize();
		}

		Set<IsConstraintOf> constrEdges = new HashSet<IsConstraintOf>();
		for (IsConstraintOf edge : syntaxgraph.getIsConstraintOfEdges()) {
			constrEdges.add(edge);
		}
		SemanticGraph sg, miniSG;
		Declaration decl;
		for (IsConstraintOf constraint : constrEdges) {
			decl = (Declaration) constraint.getOmega();
			sg = SemanticGraph.createSemanticGraphFromExpression(
					(Expression) constraint.getAlpha(), eval
							.getVertexEvaluatorGraphMarker(), graphSize);
			miniSG = sg.dissolve();

			int sgCosts = sg.getCosts();
			int miniSGCosts = miniSG.getCosts();

			Expression optimizedExpression;

			if (miniSGCosts < sgCosts) {
				System.out.println(optimizerHeaderString()
						+ "Minimized formula\n    " + sg + "\nto\n    "
						+ miniSG);
				optimizedExpression = miniSG.toExpression(syntaxgraph);
			} else {
				System.out.println(optimizerHeaderString()
						+ "Minimization result costs " + miniSGCosts
						+ " while original formula costs " + sgCosts
						+ ", so the original one is used.");
				optimizedExpression = sg.toExpression(syntaxgraph);
			}

			syntaxgraph.createIsConstraintOf(optimizedExpression, decl);
			try {
				eval.createVertexEvaluators();
			} catch (EvaluateException e) {
				e.printStackTrace();
				throw new OptimizerException(
						"Exception while re-creating VertexEvaluators.", e);
			}
		}
	}
}
