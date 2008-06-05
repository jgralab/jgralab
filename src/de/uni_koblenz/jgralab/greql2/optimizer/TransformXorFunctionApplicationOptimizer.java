/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.funlib.Xor;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;

/**
 * Replaces all {@link Xor} {@link FunctionApplication}s in the {@link Greql2}
 * graph according the rule
 * <code>a xor b = (a and not b) or (not a and b)</code>.
 * 
 * @author Tassilo Horn (horn), 2008
 * 
 */
public class TransformXorFunctionApplicationOptimizer extends OptimizerBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz.jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof TransformXorFunctionApplicationOptimizer) {
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
		ArrayList<FunctionApplication> xors = new ArrayList<FunctionApplication>();
		for (FunctionApplication funApp : syntaxgraph
				.getFunctionApplicationVertices()) {
			if (OptimizerUtility.isXor(funApp)) {
				xors.add(funApp);
			}
		}
		boolean somethingWasTransformed = false;
		for (FunctionApplication xor : xors) {
			somethingWasTransformed = true;
			// Figure out the two arguments of the Xor
			IsArgumentOf isArgOf = xor.getFirstIsArgumentOf(EdgeDirection.IN);
			Expression arg1 = (Expression) isArgOf.getAlpha();
			isArgOf = isArgOf.getNextIsArgumentOf(EdgeDirection.IN);
			Expression arg2 = (Expression) isArgOf.getAlpha();

			// The rule is: a xor b = a and ~b or ~a and b

			// create the top-level Or
			FunctionApplication or = syntaxgraph.createFunctionApplication();
			FunctionId orId = OptimizerUtility.findOrCreateFunctionId("or",
					syntaxgraph);
			syntaxgraph.createIsFunctionIdOf(orId, or);

			// create the two Ands needed in the replacement
			FunctionApplication leftAnd = syntaxgraph
					.createFunctionApplication();
			FunctionApplication rightAnd = syntaxgraph
					.createFunctionApplication();
			FunctionId andId = OptimizerUtility.findOrCreateFunctionId("and",
					syntaxgraph);
			syntaxgraph.createIsFunctionIdOf(andId, leftAnd);
			syntaxgraph.createIsFunctionIdOf(andId, rightAnd);

			// create the two Nots
			FunctionApplication leftNot = syntaxgraph
					.createFunctionApplication();
			FunctionApplication rightNot = syntaxgraph
					.createFunctionApplication();
			FunctionId notId = OptimizerUtility.findOrCreateFunctionId("not",
					syntaxgraph);
			syntaxgraph.createIsFunctionIdOf(notId, leftNot);
			syntaxgraph.createIsFunctionIdOf(notId, rightNot);

			// connect all vertices
			syntaxgraph.createIsArgumentOf(leftAnd, or);
			syntaxgraph.createIsArgumentOf(rightAnd, or);

			syntaxgraph.createIsArgumentOf(arg1, leftAnd);
			syntaxgraph.createIsArgumentOf(leftNot, leftAnd);
			syntaxgraph.createIsArgumentOf(arg2, leftNot);

			syntaxgraph.createIsArgumentOf(arg1, rightNot);
			syntaxgraph.createIsArgumentOf(rightNot, rightAnd);
			syntaxgraph.createIsArgumentOf(arg2, rightAnd);

			// relink all edges that started in the Xor vertex
			ArrayList<Edge> edgesToBeRelinked = new ArrayList<Edge>();
			Edge e = xor.getFirstEdge(EdgeDirection.OUT);
			while (e != null) {
				edgesToBeRelinked.add(e);
				e = e.getNextEdge(EdgeDirection.OUT);
			}
			for (Edge edge : edgesToBeRelinked) {
				edge.setAlpha(or);
			}

			// delete the Xor
			xor.delete();
		}

		return somethingWasTransformed;
	}

}
