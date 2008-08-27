/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.*;
import de.uni_koblenz.jgralab.greql2.exception.*;
import de.uni_koblenz.jgralab.greql2.jvalue.*;
import de.uni_koblenz.jgralab.*;

/**
 * Evaluates an exponentiated path description. Creates a NFA that accepts the
 * exponentiated path description.
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
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
	public Vertex getVertex() {
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
		PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(p);
		VertexEvaluator exponentEvaluator = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(vertex
				.getFirstIsExponentOf(EdgeDirection.IN).getAlpha());
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
		return new JValue(NFA.createExponentiatedPathDescriptionNFA(pathEval
				.getNFA(), exponent));
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsExponentiatedPathDescription(this, graphSize);
	}

}
