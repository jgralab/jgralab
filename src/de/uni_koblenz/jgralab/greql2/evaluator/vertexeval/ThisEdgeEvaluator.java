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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.ThisEdge;

/**
 * Evaluates a ThisEdge vertex in the GReQL-2 Syntaxgraph.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ThisEdgeEvaluator extends VariableEvaluator {

	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public ThisEdgeEvaluator(ThisEdge vertex, GreqlEvaluator eval) {
		super(vertex, eval);
		// this.vertex = vertex;
	}

	// calculates the set of depending expressions of this evaluator, but using
	// a fs-approach
	// which stops at the first path description of each path
	@Override
	protected List<VertexEvaluator> calculateDependingExpressions() {
		Queue<Greql2Vertex> queue = new LinkedList<Greql2Vertex>();
		List<VertexEvaluator> dependingEvaluators = new ArrayList<VertexEvaluator>();
		queue.add(vertex);
		while (!queue.isEmpty()) {
			Greql2Vertex currentVertex = queue.poll();
			VertexEvaluator eval = vertexEvalMarker.getMark(currentVertex);

			if ((eval != null) && (!dependingEvaluators.contains(eval))
					&& (!(eval instanceof PathDescriptionEvaluator))
					&& (!(eval instanceof DeclarationEvaluator))
					&& (!(eval instanceof SimpleDeclarationEvaluator))) {
				dependingEvaluators.add(eval);
			}
			Greql2Aggregation currentEdge = currentVertex
					.getFirstGreql2AggregationIncidence(EdgeDirection.OUT);
			while (currentEdge != null) {
				Greql2Vertex nextVertex = (Greql2Vertex) currentEdge.getThat();
				if (!(nextVertex instanceof PathDescription)) {
					queue.add(nextVertex);
				}
				currentEdge = currentEdge
						.getNextGreql2Aggregation(EdgeDirection.OUT);
			}
		}
		return dependingEvaluators;
	}

}
