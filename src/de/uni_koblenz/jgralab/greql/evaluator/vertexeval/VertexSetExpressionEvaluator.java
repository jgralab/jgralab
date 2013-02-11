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

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.schema.IsTypeRestrOfExpression;
import de.uni_koblenz.jgralab.greql.schema.VertexSetExpression;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;

/**
 * construct a subset of the datagraph vertices. For instance, the expression
 * V:{Department} will be evaluated by this evaluator, it will construct the set
 * of vertices in the datagraph that have the type Department or a type that is
 * derived from Department
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VertexSetExpressionEvaluator extends
		ElementSetExpressionEvaluator<VertexSetExpression> {

	public VertexSetExpressionEvaluator(VertexSetExpression vertex,
			GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		TypeCollection tc = getTypeCollection(evaluator);
		PSet<Vertex> resultSet = null;
		if (resultSet == null) {
			resultSet = JGraLab.set();
			Vertex currentVertex = evaluator.getGraph().getFirstVertex();
			while (currentVertex != null) {
				if (tc.acceptsType(currentVertex.getAttributedElementClass())) {
					resultSet = resultSet.plus(currentVertex);
				}
				currentVertex = currentVertex.getNextVertex();
			}
		}
		evaluator.progress(getOwnEvaluationCosts());
		return resultSet;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		VertexSetExpression vse = getVertex();

		long typeRestrCosts = 0;
		IsTypeRestrOfExpression inc = vse
				.getFirstIsTypeRestrOfExpressionIncidence();
		while (inc != null) {
			TypeIdEvaluator tideval = (TypeIdEvaluator) query
					.getVertexEvaluator(inc.getAlpha());
			typeRestrCosts += tideval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsTypeRestrOfExpressionIncidence();
		}

		long ownCosts = query.getOptimizer().getOptimizerInfo()
				.getAverageVertexCount();
		return new VertexCosts(ownCosts, ownCosts, typeRestrCosts + ownCosts);
	}

	@Override
	public long calculateEstimatedCardinality() {
		long card;
		if (typeCollection != null) {
			card = typeCollection.getEstimatedGraphElementCount(query
					.getOptimizer().getOptimizerInfo());
		} else {
			VertexSetExpression exp = getVertex();
			IsTypeRestrOfExpression inc = exp
					.getFirstIsTypeRestrOfExpressionIncidence();
			double selectivity = 1.0;
			if (inc != null) {
				TypeIdEvaluator typeIdEval = (TypeIdEvaluator) query
						.getVertexEvaluator(inc.getAlpha());
				selectivity = typeIdEval.getEstimatedSelectivity();
			}
			card = Math.round(query.getOptimizer().getOptimizerInfo()
					.getAverageVertexCount()
					* selectivity);
		}
		logger.fine("VertexSet estimated cardinality " + typeCollection + ": "
				+ card);
		return card;
	}

}
