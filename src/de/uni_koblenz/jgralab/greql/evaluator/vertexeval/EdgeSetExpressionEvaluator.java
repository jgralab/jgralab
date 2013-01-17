/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql.evaluator.vertexeval;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.EdgeSetExpression;
import de.uni_koblenz.jgralab.greql.schema.IsTypeRestrOfExpression;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * Calculates a subset of the datagraph edges
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EdgeSetExpressionEvaluator extends
		ElementSetExpressionEvaluator<EdgeSetExpression> {

	public EdgeSetExpressionEvaluator(EdgeSetExpression vertex,
			GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	public PSet<Edge> evaluate(InternalGreqlEvaluator evaluator) {
		TypeCollection tc = getTypeCollection(evaluator);
		// create the resulting set
		PSet<Edge> resultSet = JGraLab.set();
		Edge currentEdge = evaluator.getGraph().getFirstEdge();
		while (currentEdge != null) {
			EdgeClass edgeClass = currentEdge.getAttributedElementClass();
			if (tc.acceptsType(edgeClass)) {
				resultSet = resultSet.plus(currentEdge);
			}
			currentEdge = currentEdge.getNextEdge();
		}
		evaluator.progress(getOwnEvaluationCosts());
		return resultSet;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		EdgeSetExpression ese = getVertex();

		long typeRestrCosts = 0;
		IsTypeRestrOfExpression inc = ese
				.getFirstIsTypeRestrOfExpressionIncidence();
		while (inc != null) {
			TypeIdEvaluator tideval = (TypeIdEvaluator) query
					.getVertexEvaluator(inc.getAlpha());
			typeRestrCosts += tideval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsTypeRestrOfExpressionIncidence();
		}

		long ownCosts = query.getOptimizer().getOptimizerInfo()
				.getAverageEdgeCount();
		return new VertexCosts(ownCosts, ownCosts, typeRestrCosts + ownCosts);
	}

	@Override
	public long calculateEstimatedCardinality() {
		long card;
		if (typeCollection != null) {
			card = typeCollection.getEstimatedGraphElementCount(query
					.getOptimizer().getOptimizerInfo());
		} else {
			EdgeSetExpression exp = getVertex();
			IsTypeRestrOfExpression inc = exp
					.getFirstIsTypeRestrOfExpressionIncidence();
			double selectivity = 1.0;
			if (inc != null) {
				TypeIdEvaluator typeIdEval = (TypeIdEvaluator) query
						.getVertexEvaluator(inc.getAlpha());
				selectivity = typeIdEval.getEstimatedSelectivity();
			}
			card = Math.round(query.getOptimizer().getOptimizerInfo()
					.getAverageEdgeCount()
					* selectivity);
		}
		logger.fine("EdgeSet estimated cardinality " + typeCollection + ": "
				+ card);
		return card;
	}
}
