/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsSequenceElementOf;
import de.uni_koblenz.jgralab.greql2.schema.SequentialPathDescription;

public class SequentialPathDescriptionEvaluator extends
		PathDescriptionEvaluator {

	/**
	 * The SequentialPathDescription-Vertex this evaluator evaluates
	 */
	private SequentialPathDescription vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
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
	public SequentialPathDescriptionEvaluator(SequentialPathDescription vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		IsSequenceElementOf inc = vertex
				.getFirstIsSequenceElementOf(EdgeDirection.IN);
		ArrayList<NFA> nfaList = new ArrayList<NFA>();
		while (inc != null) {
			PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			nfaList.add(pathEval.getNFA());
			inc = inc.getNextIsSequenceElementOf(EdgeDirection.IN);
		}
		return new JValue(NFA.createSequentialPathDescriptionNFA(nfaList));
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsSequentialPathDescription(this, graphSize);
	}

}
