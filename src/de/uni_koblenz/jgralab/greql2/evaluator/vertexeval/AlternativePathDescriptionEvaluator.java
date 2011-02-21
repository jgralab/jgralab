/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.schema.AlternativePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsAlternativePathOf;

/**
 * Evaluates an alternative path description. Creates a NFA that accepts the
 * alternative path description.
 * 
 * @author ist@uni-koblenz.de
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
	public Greql2Vertex getVertex() {
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
				.getFirstIsAlternativePathOfIncidence(EdgeDirection.IN);
		ArrayList<NFA> nfaList = new ArrayList<NFA>();
		while (inc != null) {
			PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) vertexEvalMarker
					.getMark(inc.getAlpha());
			nfaList.add(pathEval.getNFA());
			inc = inc.getNextIsAlternativePathOf(EdgeDirection.IN);
		}
		return new JValueImpl(NFA.createAlternativePathDescriptionNFA(nfaList));
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsAlternativePathDescription(this, graphSize);
	}

}
