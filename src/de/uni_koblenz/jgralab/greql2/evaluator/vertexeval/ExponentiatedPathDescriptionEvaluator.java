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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;
import de.uni_koblenz.jgralab.greql2.schema.ExponentiatedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IntLiteral;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;

/**
 * Evaluates an exponentiated path description. Creates a NFA that accepts the
 * exponentiated path description.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ExponentiatedPathDescriptionEvaluator extends
		PathDescriptionEvaluator<ExponentiatedPathDescription> {

	/**
	 * the default exponent that is used if the exponent of an exponentiated
	 * path description is not an integer literal but some complex expression
	 */
	protected static final int defaultExponent = 3;

	/**
	 * Creates a new ExponentiatedPathDescriptionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public ExponentiatedPathDescriptionEvaluator(
			ExponentiatedPathDescription vertex, QueryImpl query) {
		super(vertex, query);
	}

	@Override
	public NFA evaluate(InternalGreqlEvaluator evaluator) {
		evaluator.progress(getOwnEvaluationCosts());
		PathDescription p = (PathDescription) vertex
				.getFirstIsExponentiatedPathOfIncidence().getAlpha();
		PathDescriptionEvaluator<?> pathEval = (PathDescriptionEvaluator<?>) query
				.getVertexEvaluator(p);
		VertexEvaluator<? extends Expression> exponentEvaluator = query
				.getVertexEvaluator((Expression) vertex
						.getFirstIsExponentOfIncidence(EdgeDirection.IN)
						.getAlpha());
		Object exponentValue = exponentEvaluator.getResult(evaluator);
		int exponent = 0;
		if (exponentValue instanceof Integer) {
			exponent = (Integer) exponentValue;
		} else {
			throw new GreqlException(
					"Exponent of ExponentiatedPathDescription is not convertable to integer value");
		}
		if (exponent <= 0) {
			throw new GreqlException(
					"Exponent of ExponentiatedPathDescription is " + exponent
							+ " but must be >=1.");
		}
		return NFA.createExponentiatedPathDescriptionNFA(
				pathEval.getNFA(evaluator), exponent);
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		ExponentiatedPathDescription p = getVertex();
		long exponent = defaultExponent;
		VertexEvaluator<IntLiteral> expEval = query
				.getVertexEvaluator((IntLiteral) p
						.getFirstIsExponentOfIncidence(EdgeDirection.IN)
						.getAlpha());
		if (expEval instanceof IntLiteralEvaluator) {
			try {
				exponent = ((Number) expEval.getResult(null)).longValue();
			} catch (Exception ex) {
			}
		}
		long exponentCosts = expEval.getCurrentSubtreeEvaluationCosts();
		VertexEvaluator<? extends PathDescription> pathEval = query
				.getVertexEvaluator((PathDescription) p
						.getFirstIsExponentiatedPathOfIncidence(
								EdgeDirection.IN).getAlpha());
		long pathCosts = pathEval.getCurrentSubtreeEvaluationCosts();
		long ownCosts = ((pathCosts * exponent) * 1) / 3;
		long subtreeCosts = pathCosts + ownCosts + exponentCosts;
		return new VertexCosts(ownCosts, ownCosts, subtreeCosts);
	}

}
