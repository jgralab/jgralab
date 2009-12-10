/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.optimizer.condexp.Formula;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.Literal;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ConditionalExpressionOptimizer extends OptimizerBase {

	private static Logger logger = JGraLab
			.getLogger(ConditionalExpressionOptimizer.class.getPackage()
					.getName());

	private static class VertexEdgeClassTuple {
		public VertexEdgeClassTuple(Greql2Vertex v, Class<? extends Edge> ec) {
			this.v = v;
			this.ec = ec;
		}

		Greql2Vertex v;
		Class<? extends Edge> ec;
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
		if (optimizer instanceof ConditionalExpressionOptimizer) {
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
		Formula.setSimplifiedOrOptimized(false);

		FunctionApplication top = findAndOrNotFunApp(syntaxgraph
				.getFirstGreql2Expression());
		while (top != null) {
			LinkedList<VertexEdgeClassTuple> relinkables = rememberConnections(top);
			Formula formula = Formula.createFormulaFromExpression(top, eval);
			top.delete();
			Formula optimizedFormula = formula.simplify().optimize();
			if (!formula.equals(optimizedFormula)) {
				Formula.setSimplifiedOrOptimized(true);
				logger.fine(optimizerHeaderString()
						+ "Transformed constraint\n    " + formula
						+ "\nto\n    " + optimizedFormula + ".");
				Greql2Vertex newTop = optimizedFormula.toExpression();
				for (VertexEdgeClassTuple vect : relinkables) {
					syntaxgraph.createEdge(vect.ec, newTop, vect.v);
				}
				top = findAndOrNotFunApp(syntaxgraph.getFirstGreql2Expression());
			}
		}

		try {
			eval.createVertexEvaluators();
		} catch (EvaluateException e) {
			e.printStackTrace();
			throw new OptimizerException(
					"Exception while re-creating VertexEvaluators.", e);
		}

		OptimizerUtility.createMissingSourcePositions(syntaxgraph);
		return Formula.isSimplifiedOrOptimized();
	}

	@SuppressWarnings("unchecked")
	private LinkedList<VertexEdgeClassTuple> rememberConnections(
			FunctionApplication top) {
		LinkedList<VertexEdgeClassTuple> list = new LinkedList<VertexEdgeClassTuple>();
		for (Edge e : top.incidences(EdgeDirection.OUT)) {
			list.add(new VertexEdgeClassTuple((Greql2Vertex) e.getOmega(),
					(Class<? extends Edge>) e.getM1Class()));
		}
		return list;
	}

	private FunctionApplication findAndOrNotFunApp(Greql2Expression g) {
		Queue<Greql2Vertex> queue = new LinkedList<Greql2Vertex>();
		queue.add(g);
		while (!queue.isEmpty()) {
			Greql2Vertex v = queue.poll();
			if (v instanceof FunctionApplication) {
				FunctionApplication f = (FunctionApplication) v;
				if (OptimizerUtility.isAnd(f) || OptimizerUtility.isOr(f)
						|| OptimizerUtility.isNot(f)) {
					// The conditional expression evaluator optimizes till
					// only expressions with at most one non-complex term stay,
					// so return only FunApps which have 2 or more non-literals
					// as args. Additionally, the non-constant terms have to be
					// different, so that ((Null | v7) & ~v7) is not recognized.
					HashSet<Vertex> nonConsts = new HashSet<Vertex>();
					collectNonCostantTerms(f, nonConsts);
					// System.out.println("nCT(" + f + ") = " + nonConsts);
					if (nonConsts.size() >= 2) {
						return f;
					}
				}
			}
			for (Edge e : v.incidences(EdgeDirection.IN)) {
				queue.offer((Greql2Vertex) e.getAlpha());
			}
		}
		return null;
	}

	private void collectNonCostantTerms(Vertex f, HashSet<Vertex> nonConsts) {
		for (Edge e : f.incidences(EdgeDirection.IN)) {
			Vertex v = e.getAlpha();
			if (v instanceof FunctionApplication) {
				FunctionApplication funApp = (FunctionApplication) v;
				if (OptimizerUtility.isAnd(funApp)
						|| OptimizerUtility.isOr(funApp)
						|| OptimizerUtility.isNot(funApp)) {
					collectNonCostantTerms(v, nonConsts);
				} else {
					nonConsts.add(v);
				}
			} else if ((v instanceof Literal) || (v instanceof FunctionId)) {
				// Those are uninteresting vertices. Skip em!
				continue;
			} else {
				nonConsts.add(v);
			}
		}
	}
}
