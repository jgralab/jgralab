/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.IntermediateVertexPathDescription;
import de.uni_koblenz.jgralab.greql.schema.IsSubPathOf;
import de.uni_koblenz.jgralab.greql.schema.PathDescription;

/**
 * Evaluates an IntermediateVertexPathDescription.
 * 
 * @author ist@uni-koblenz.de Summer 2006, Diploma Thesis
 * 
 */
public class IntermediateVertexPathDescriptionEvaluator extends
		PathDescriptionEvaluator<IntermediateVertexPathDescription> {

	/**
	 * Creates a new IntermediateVertexPathDescriptionEvaluator for the given
	 * vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public IntermediateVertexPathDescriptionEvaluator(
			IntermediateVertexPathDescription vertex, QueryImpl query) {
		super(vertex, query);
	}

	@Override
	public NFA evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		IsSubPathOf inc = vertex.getFirstIsSubPathOfIncidence(EdgeDirection.IN);
		PathDescriptionEvaluator<?> firstEval = (PathDescriptionEvaluator<?>) query
				.getVertexEvaluator((PathDescription) inc.getAlpha());
		NFA firstNFA = firstEval.getNFA(evaluator);
		inc = inc.getNextIsSubPathOfIncidence(EdgeDirection.IN);
		PathDescriptionEvaluator<?> secondEval = (PathDescriptionEvaluator<?>) query
				.getVertexEvaluator((PathDescription) inc.getAlpha());
		NFA secondNFA = secondEval.getNFA(evaluator);
		VertexEvaluator<? extends Expression> vertexEval = query
				.getVertexEvaluator((Expression) vertex
						.getFirstIsIntermediateVertexOfIncidence(
								EdgeDirection.IN).getAlpha());
		return NFA.createIntermediateVertexPathDescriptionNFA(firstNFA,
				vertexEval, secondNFA);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		IntermediateVertexPathDescription pathDesc = getVertex();
		IsSubPathOf inc = pathDesc.getFirstIsSubPathOfIncidence();
		PathDescriptionEvaluator<? extends PathDescription> firstPathEval = (PathDescriptionEvaluator<? extends PathDescription>) query
				.getVertexEvaluator((PathDescription) inc.getAlpha());
		inc = inc.getNextIsSubPathOfIncidence();
		PathDescriptionEvaluator<? extends PathDescription> secondPathEval = (PathDescriptionEvaluator<? extends PathDescription>) query
				.getVertexEvaluator((PathDescription) inc.getAlpha());
		long firstCosts = firstPathEval.getCurrentSubtreeEvaluationCosts();
		long secondCosts = secondPathEval.getCurrentSubtreeEvaluationCosts();
		VertexEvaluator<? extends Expression> vertexEval = query
				.getVertexEvaluator((Expression) pathDesc
						.getFirstIsIntermediateVertexOfIncidence().getAlpha());
		long intermVertexCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		long ownCosts = 10;
		long iteratedCosts = 10;
		long subtreeCosts = iteratedCosts + intermVertexCosts + firstCosts
				+ secondCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

}
