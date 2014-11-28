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

package de.uni_koblenz.jgralab.greql.evaluator.vertexeval;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql.schema.EdgePathDescription;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.IsEdgeRestrOf;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;

/**
 * Creates a NFA wich accepts a single edge out of the --edge-> - clause
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EdgePathDescriptionEvaluator extends
		PrimaryPathDescriptionEvaluator<EdgePathDescription> {

	public EdgePathDescriptionEvaluator(EdgePathDescription vertex,
			GreqlQueryImpl query) {
		super(vertex, query);
	}

	@Override
	public NFA evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		Edge evalEdge = vertex.getFirstIsEdgeExprOfIncidence();
		VertexEvaluator<? extends Expression> edgeEval = null;
		if (evalEdge != null) {
			edgeEval = query.getVertexEvaluator((Expression) evalEdge
					.getAlpha());
		}
		TypeCollection typeCollection = TypeCollection.empty();
		// IsTypeRestrOfExpression inc = vertex
		// .getFirstIsTypeRestrOfExpressionIncidence(EdgeDirection.IN);
		IsEdgeRestrOf inc = vertex
				.getFirstIsEdgeRestrOfIncidence(EdgeDirection.IN);
		EdgeRestrictionEvaluator edgeRestEval = null;
		VertexEvaluator<? extends Expression> predicateEvaluator = null;
		if (inc != null) {
			edgeRestEval = (EdgeRestrictionEvaluator) query
					.getVertexEvaluator(inc.getAlpha());
			typeCollection = typeCollection.combine(edgeRestEval
					.getTypeCollection(evaluator));
			predicateEvaluator = edgeRestEval.getPredicateEvaluator();
		}
		try {
			typeCollection = typeCollection.bindToSchema(evaluator);
		} catch (UnknownTypeException e) {
			throw new UnknownTypeException(e.getTypeName(),
					createPossibleSourcePositions());
		}
		NFA createdNFA = NFA
				.createEdgePathDescriptionNFA(getEdgeDirection(vertex),
						typeCollection, getEdgeRoles(edgeRestEval), edgeEval,
						predicateEvaluator, query);
		evaluator.setLocalEvaluationResult(vertex, createdNFA);
		return createdNFA;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		EdgePathDescription edgePathDesc = getVertex();
		VertexEvaluator<? extends Expression> edgeEval = query
				.getVertexEvaluator(edgePathDesc
						.getFirstIsEdgeExprOfIncidence().getAlpha());
		long edgeCosts = edgeEval.getCurrentSubtreeEvaluationCosts();
		return new VertexCosts(transitionCosts, transitionCosts,
				transitionCosts + edgeCosts);
	}

}
