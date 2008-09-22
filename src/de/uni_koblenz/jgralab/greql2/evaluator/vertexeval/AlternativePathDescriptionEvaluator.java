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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.AlternativePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.IsAlternativePathOf;

/**
 * Evaluates an alternative path description. Creates a NFA that accepts the
 * alternative path description.
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class AlternativePathDescriptionEvaluator extends
		PathDescriptionEvaluator {

	/**
	 * The AlternativePathDescription-Vertex this evaluator evaluates
	 */
	private AlternativePathDescription vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * Creates a new IteratedPathDescriptionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public AlternativePathDescriptionEvaluator(
			AlternativePathDescription vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/**
	 * Calculate the costs to re-evaluate this vertex if the given variable
	 * changes. Assumes, that also the variables "right" of the given one
	 * change.
	 */

	@Override
	public JValue evaluate() throws EvaluateException {
		IsAlternativePathOf inc = vertex
				.getFirstIsAlternativePathOf(EdgeDirection.IN);
		ArrayList<NFA> nfaList = new ArrayList<NFA>();
		while (inc != null) {
			PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(inc
					.getAlpha());
			nfaList.add(pathEval.getNFA());
			inc = inc.getNextIsAlternativePathOf(EdgeDirection.IN);
		}
		return new JValue(NFA.createAlternativePathDescriptionNFA(nfaList));
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsAlternativePathDescription(this, graphSize);
	}

}