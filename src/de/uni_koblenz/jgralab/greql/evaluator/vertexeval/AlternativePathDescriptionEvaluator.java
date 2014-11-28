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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql.schema.AlternativePathDescription;
import de.uni_koblenz.jgralab.greql.schema.IsAlternativePathOf;
import de.uni_koblenz.jgralab.greql.schema.PathDescription;

/**
 * Evaluates an alternative path description. Creates a NFA that accepts the
 * alternative path description.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class AlternativePathDescriptionEvaluator extends
		PathDescriptionEvaluator<AlternativePathDescription> {

	public AlternativePathDescriptionEvaluator(
			AlternativePathDescription vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	/**
	 * Calculate the costs to re-evaluate this vertex if the given variable
	 * changes. Assumes, that also the variables "right" of the given one
	 * change.
	 */

	@Override
	public NFA evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		IsAlternativePathOf inc = vertex
				.getFirstIsAlternativePathOfIncidence(EdgeDirection.IN);
		ArrayList<NFA> nfaList = new ArrayList<>();
		while (inc != null) {
			PathDescriptionEvaluator<?> pathEval = (PathDescriptionEvaluator<?>) query
					.getVertexEvaluator(inc.getAlpha());
			nfaList.add(pathEval.getNFA(evaluator));
			inc = inc.getNextIsAlternativePathOfIncidence(EdgeDirection.IN);
		}
		return NFA.createAlternativePathDescriptionNFA(nfaList);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		AlternativePathDescription p = getVertex();
		long aggregatedCosts = 0;
		IsAlternativePathOf inc = p
				.getFirstIsAlternativePathOfIncidence(EdgeDirection.IN);
		long alternatives = 0;
		while (inc != null) {
			PathDescriptionEvaluator<? extends PathDescription> pathEval = (PathDescriptionEvaluator<? extends PathDescription>) query
					.getVertexEvaluator(inc.getAlpha());
			aggregatedCosts += pathEval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsAlternativePathOfIncidence(EdgeDirection.IN);
			alternatives++;
		}
		aggregatedCosts += 10 * alternatives;
		return new VertexCosts(10 * alternatives, 10 * alternatives,
				aggregatedCosts);
	}

}
