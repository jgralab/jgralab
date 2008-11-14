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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ValueConstruction;

/**
 * This is the abstract base class for all ValueConstructions
 * 
 * @author ist@uni-koblenz.de
 * 
 */
abstract public class ConstructionEvaluator extends VertexEvaluator {

	ValueConstruction vertex;

	public ConstructionEvaluator(ValueConstruction vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	public JValue createValue(JValueCollection collection)
			throws EvaluateException {
		Edge edge = vertex.getFirstIsPartOf();
		while (edge != null) {
			Expression currentExpression = (Expression) edge.getAlpha();
			VertexEvaluator vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(currentExpression);
			collection.add(vertexEval.getResult(subgraph));
			edge = edge.getNextEdge();
		}
		return collection;
	}

}
