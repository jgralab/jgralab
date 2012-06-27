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
import de.uni_koblenz.jgralab.greql.schema.PathDescription;
import de.uni_koblenz.jgralab.greql.schema.TransposedPathDescription;

/**
 * Evaluates a TransposedPathDescription vertex. Creates a NFA, which accepts
 * the PathDescription the vertex describes.
 * 
 * @author ist@uni-koblenz.de Summer 2006, Diploma Thesis
 * 
 */
public class TransposedPathDescriptionEvaluator extends
		PathDescriptionEvaluator<TransposedPathDescription> {

	/**
	 * Creates a new TransposedPathDescriptionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public TransposedPathDescriptionEvaluator(TransposedPathDescription vertex,
			QueryImpl query) {
		super(vertex, query);
	}

	@Override
	public NFA evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		PathDescription p = (PathDescription) vertex
				.getFirstIsTransposedPathOfIncidence(EdgeDirection.IN)
				.getAlpha();
		PathDescriptionEvaluator<?> pathEval = (PathDescriptionEvaluator<?>) query
				.getVertexEvaluator(p);
		return NFA.createTransposedPathDescriptionNFA(pathEval
				.getNFA(evaluator));
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		TransposedPathDescription transPath = getVertex();
		PathDescriptionEvaluator<? extends PathDescription> pathEval = (PathDescriptionEvaluator<? extends PathDescription>) query
				.getVertexEvaluator((PathDescription) transPath
						.getFirstIsTransposedPathOfIncidence().getAlpha());
		long pathCosts = pathEval.getCurrentSubtreeEvaluationCosts();
		long transpositionCosts = pathCosts / 20;
		long subtreeCosts = transpositionCosts + pathCosts;
		return new VertexCosts(transpositionCosts, transpositionCosts,
				subtreeCosts);
	}

}
