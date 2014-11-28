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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql.optimizer.condexp.Formula;
import de.uni_koblenz.jgralab.greql.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ConditionalExpressionOptimizer extends OptimizerBase {

	public ConditionalExpressionOptimizer(OptimizerInfo optimizerInfo) {
		super(optimizerInfo);
	}

	private static Logger logger = JGraLab
			.getLogger(ConditionalExpressionOptimizer.class);

	private static class VertexEdgeClassTuple {
		public VertexEdgeClassTuple(GreqlVertex v, EdgeClass ec) {
			this.v = v;
			this.ec = ec;
		}

		GreqlVertex v;
		EdgeClass ec;
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
	 * de.uni_koblenz.jgralab.greql2.schema.Greql)
	 */
	@Override
	public boolean optimize(GreqlQuery query) throws OptimizerException {
		boolean simplifiedOrOptimized = false;
		// System.out.println("Before CEO: "
		// + GreqlSerializer.serializeGraph(syntaxgraph));

		FunctionApplication top = findAndOrNotFunApp(query.getQueryGraph()
				.getFirstGreqlExpression());
		while (top != null) {
			LinkedList<VertexEdgeClassTuple> relinkables = rememberConnections(top);
			Formula formula = Formula.createFormulaFromExpression(top, query);
			// System.out.println("Formula = " + formula);
			Formula optimizedFormula = formula.simplify().optimize();
			if (!formula.equals(optimizedFormula)) {
				simplifiedOrOptimized = true;
				logger.fine(optimizerHeaderString()
						+ "Transformed constraint\n    " + formula
						+ "\nto\n    " + optimizedFormula + ".");
				GreqlVertex newTop = optimizedFormula.toExpression();
				for (VertexEdgeClassTuple vect : relinkables) {
					query.getQueryGraph().createEdge(vect.ec, newTop, vect.v);
				}
				top.delete();
				top = findAndOrNotFunApp(query.getQueryGraph()
						.getFirstGreqlExpression());
			} else {
				top = null;
			}
		}

		// delete "with true" constraints
		Set<Vertex> verticesToDelete = new HashSet<>();
		for (IsConstraintOf ico : query.getQueryGraph()
				.getIsConstraintOfEdges()) {
			Vertex alpha = ico.getAlpha();
			if (alpha instanceof BoolLiteral) {
				BoolLiteral bl = (BoolLiteral) alpha;
				if (bl.is_boolValue()) {
					verticesToDelete.add(bl);
				}
			}
		}
		for (Vertex bl : verticesToDelete) {
			bl.delete();
		}

		OptimizerUtility.createMissingSourcePositions(query.getQueryGraph());

		// System.out.println("After CEO: "
		// + GreqlSerializer.serializeGraph(syntaxgraph));
		// Tg2Dot.printGraphAsDot(syntaxgraph, true, "/home/horn/ceo.dot");

		return simplifiedOrOptimized;
	}

	private LinkedList<VertexEdgeClassTuple> rememberConnections(
			FunctionApplication top) {
		LinkedList<VertexEdgeClassTuple> list = new LinkedList<>();
		assert top.isValid();
		for (Edge e : top.incidences(EdgeDirection.OUT)) {
			list.add(new VertexEdgeClassTuple((GreqlVertex) e.getOmega(), e
					.getAttributedElementClass()));
		}
		return list;
	}

	private FunctionApplication findAndOrNotFunApp(GreqlExpression g) {
		Queue<GreqlVertex> queue = new LinkedList<>();
		queue.add(g);
		while (!queue.isEmpty()) {
			GreqlVertex v = queue.poll();
			if (v instanceof FunctionApplication) {
				FunctionApplication f = (FunctionApplication) v;
				if (OptimizerUtility.isAnd(f) || OptimizerUtility.isOr(f)
						|| OptimizerUtility.isNot(f)) {
					return f;
				}
			}
			for (Edge e : v.incidences(EdgeDirection.IN)) {
				queue.offer((GreqlVertex) e.getAlpha());
			}
		}
		return null;
	}

}
