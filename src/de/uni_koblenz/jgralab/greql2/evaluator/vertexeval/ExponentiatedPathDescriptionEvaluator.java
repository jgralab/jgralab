/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.schema.ExponentiatedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;

/**
 * Evaluates an exponentiated path description. Creates a NFA that accepts the
 * exponentiated path description.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ExponentiatedPathDescriptionEvaluator extends
		PathDescriptionEvaluator {

	/**
	 * The ExponentiatedPathDescription-Vertex this evaluator evaluates
	 */
	private ExponentiatedPathDescription vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	/**
	 * Creates a new ExponentiatedPathDescriptionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public ExponentiatedPathDescriptionEvaluator(
			ExponentiatedPathDescription vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		PathDescription p = (PathDescription) vertex
				.getFirstIsExponentiatedPathOf().getAlpha();
		PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		VertexEvaluator exponentEvaluator = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						vertex.getFirstIsExponentOf(EdgeDirection.IN)
								.getAlpha());
		JValue exponentValue = exponentEvaluator.getResult(subgraph);
		int exponent = 0;
		if (exponentValue.isInteger()) {
			try {
				exponent = exponentValue.toInteger();
			} catch (JValueInvalidTypeException ex) {
				// cannot happen
			}
		} else {
			throw new EvaluateException(
					"Exponent of ExponentiatedPathDescription is not convertable to integer value");
		}
		return new JValueImpl(NFA.createExponentiatedPathDescriptionNFA(
				pathEval.getNFA(), exponent));
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsExponentiatedPathDescription(this, graphSize);
	}

}
