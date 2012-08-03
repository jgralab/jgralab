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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.GreqlAggregation;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.schema.PathDescription;
import de.uni_koblenz.jgralab.greql.schema.ThisVertex;

/**
 * Evaluates a Variable vertex in the GReQL-2 Syntaxgraph. Provides access to
 * the variable value using the method getResult(..), because it should make no
 * difference for other VertexEvaluators, if a vertex is root of a complex
 * subgraph or a variable. Also provides a method to set the variable value.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ThisVertexEvaluator extends VariableEvaluator<ThisVertex> {

	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public ThisVertexEvaluator(ThisVertex vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	// calculates the set of depending expressions of this evaluator, but using
	// a fs-approach
	// which stops at the first path description of each path
	@SuppressWarnings("unchecked")
	@Override
	public List<VertexEvaluator<? extends Expression>> calculateDependingExpressions() {
		Queue<GreqlVertex> queue = new LinkedList<GreqlVertex>();
		List<VertexEvaluator<? extends Expression>> dependingEvaluators = new ArrayList<VertexEvaluator<? extends Expression>>();
		queue.add(vertex);
		while (!queue.isEmpty()) {
			GreqlVertex currentVertex = queue.poll();
			VertexEvaluator<?> eval = query.getVertexEvaluator(currentVertex);

			if ((eval != null) && (!dependingEvaluators.contains(eval))
					&& (!(eval instanceof PathDescriptionEvaluator))
					&& (!(eval instanceof DeclarationEvaluator))
					&& (!(eval instanceof SimpleDeclarationEvaluator))) {
				dependingEvaluators
						.add((VertexEvaluator<? extends Expression>) eval);
			}
			GreqlAggregation currentEdge = currentVertex
					.getFirstGreqlAggregationIncidence(EdgeDirection.OUT);
			while (currentEdge != null) {
				GreqlVertex nextVertex = (GreqlVertex) currentEdge.getThat();
				if (!(nextVertex instanceof PathDescription)) {
					queue.add(nextVertex);
				}
				currentEdge = currentEdge
						.getNextGreqlAggregationIncidence(EdgeDirection.OUT);
			}
		}
		return dependingEvaluators;
	}

}
