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

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.IsPartOf;
import de.uni_koblenz.jgralab.greql.schema.ValueConstruction;

/**
 * This is the abstract base class for all ValueConstructions
 * 
 * @author ist@uni-koblenz.de
 * 
 */
abstract public class ValueConstructionEvaluator<V extends ValueConstruction>
		extends VertexEvaluator<V> {

	private ArrayList<VertexEvaluator<? extends Expression>> partEvaluators = null;

	public ValueConstructionEvaluator(V vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	public final PCollection<Object> createValue(
			PCollection<Object> collection, InternalGreqlEvaluator evaluator) {
		if (partEvaluators == null) {
			int partCount = 0;
			IsPartOf inc = vertex.getFirstIsPartOfIncidence(EdgeDirection.IN);
			while (inc != null) {
				partCount++;
				inc = inc.getNextIsPartOfIncidence(EdgeDirection.IN);
			}
			inc = vertex.getFirstIsPartOfIncidence(EdgeDirection.IN);
			partEvaluators = new ArrayList<>(partCount);
			while (inc != null) {
				Expression currentExpression = inc.getAlpha();
				VertexEvaluator<? extends Expression> vertexEval = query
						.getVertexEvaluator(currentExpression);
				partEvaluators.add(vertexEval);
				inc = inc.getNextIsPartOfIncidence(EdgeDirection.IN);
			}
		}
		for (int i = 0; i < partEvaluators.size(); i++) {
			collection = collection.plus(partEvaluators.get(i).getResult(
					evaluator));
		}
		return collection;
	}

}
